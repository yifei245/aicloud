package com.aicloud.framework.quartz.core;

import com.xxl.job.core.context.XxlJobContext;
import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import java.time.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LocalXxlJobSchedulerTest {
    @Test void twoInstancesExecuteEachScheduledOccurrenceOnlyOnceEvenAfterUnlocking() throws Exception {
        var fixture = new Fixture();
        var task = new Task();
        var scheduled = Instant.parse("2026-10-16T00:00:00Z");
        fixture.first.execute("app", "task", task, Task.class.getMethod("run"), scheduled);
        fixture.second.execute("app", "task", task, Task.class.getMethod("run"), scheduled);
        assertEquals(1, task.calls);
        fixture.second.execute("app", "task", task, Task.class.getMethod("run"), scheduled.plusSeconds(60));
        fixture.first.execute("app", "task", task, Task.class.getMethod("run"), scheduled);
        assertEquals(2, task.calls);
    }
    @Test void failedTaskCanRetryAndTheCallersJobContextIsRestored() throws Exception {
        var fixture = new Fixture();
        var task = new Task(); task.fail = true;
        var previous = new XxlJobContext(123, "previous", null, 0, 1);
        XxlJobContext.setXxlJobContext(previous);
        try {
            var scheduled = Instant.parse("2026-10-16T00:00:00Z");
            fixture.first.execute("app", "task", task, Task.class.getMethod("run"), scheduled);
            assertNull(fixture.completed.get());
            assertSame(previous, XxlJobContext.getXxlJobContext());
            task.fail = false;
            fixture.second.execute("app", "task", task, Task.class.getMethod("run"), scheduled);
            assertEquals(2, task.calls);
            assertEquals(scheduled.toEpochMilli(), fixture.completed.get());
            assertSame(previous, XxlJobContext.getXxlJobContext());
        } finally { XxlJobContext.setXxlJobContext(null); }
    }
    @Test void busyInstanceCannotExecuteTheTaskOrReleaseAnotherInstancesLock() throws Exception {
        var fixture = new Fixture(); var task = new Task();
        when(fixture.lock.tryLock()).thenReturn(false);
        when(fixture.lock.isHeldByCurrentThread()).thenReturn(false);
        fixture.first.execute("app", "task", task, Task.class.getMethod("run"), Instant.now());
        assertEquals(0, task.calls); verify(fixture.lock, never()).unlock();
    }
    public static class Task {
        int calls; boolean fail;
        public void run() {
            calls++;
            assertNotNull(XxlJobContext.getXxlJobContext());
            if (fail) XxlJobContext.getXxlJobContext().setHandleCode(XxlJobContext.HANDLE_CODE_FAIL);
        }
    }
    static class Fixture {
        final AtomicReference<Long> completed = new AtomicReference<>();
        final RLock lock = mock(RLock.class);
        final LocalXxlJobScheduler first, second;
        @SuppressWarnings("unchecked") Fixture() {
            var redis = mock(RedissonClient.class);
            RBucket<Long> bucket = mock(RBucket.class);
            when(redis.getLock(anyString())).thenReturn(lock);
            when(redis.<Long>getBucket(anyString())).thenReturn(bucket);
            when(lock.tryLock()).thenReturn(true);
            when(lock.isHeldByCurrentThread()).thenReturn(true);
            when(bucket.get()).thenAnswer(call -> completed.get());
            doAnswer(call -> { completed.set(call.getArgument(0)); return null; }).when(bucket).set(anyLong(), any(Duration.class));
            first = new LocalXxlJobScheduler(mock(ConfigurableApplicationContext.class), redis, new StandardEnvironment());
            second = new LocalXxlJobScheduler(mock(ConfigurableApplicationContext.class), redis, new StandardEnvironment());
        }
    }
}
