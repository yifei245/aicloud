package com.aicloud.framework.quartz.core;

import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.bind.*;
import org.springframework.context.*;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.Instant;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.*;

/** Reuses existing @XxlJob methods when the remote executor is disabled. */
@Slf4j
public class LocalXxlJobScheduler implements ApplicationListener<ApplicationReadyEvent>, AutoCloseable {
    private final ConfigurableApplicationContext context;
    private final RedissonClient redis;
    private final Environment environment;
    private final ThreadPoolTaskScheduler scheduler=new ThreadPoolTaskScheduler();
    private boolean initialized;
    public LocalXxlJobScheduler(ConfigurableApplicationContext context,RedissonClient redis,Environment environment) {
        this.context=context; this.redis=redis; this.environment=environment;
    }
    @Override public synchronized void onApplicationEvent(ApplicationReadyEvent event) {
        if(initialized || event.getApplicationContext()!=context) return;
        initialized=true;
        scheduler.setPoolSize(2); scheduler.setThreadNamePrefix("aicloud-local-job-"); scheduler.setDaemon(true); scheduler.initialize();
        Map<String,String> schedules=new HashMap<>();
        // State advancement and retries only; destructive log/recycle cleanup requires an explicit schedule.
        for(String name:List.of("tradeOrderAutoCancelJob","payNotifyJob","payOrderExpireJob","payOrderSyncJob",
                "payRefundSyncJob","payTransferSyncJob","couponExpireJob","combinationRecordExpireJob",
                "imRtcParticipantTimeoutJob","imRtcCallCleanupJob","deviceOfflineCheckJob","deviceUpgradeJob",
                "oaMeetingRoomBookingReminderJob","oaScheduleReminderJob","hrmEmployeeChangeJob","hrmSalaryChangeJob",
                "hrmPerformanceAppealTimeoutJob","aiMidjourneySyncJob","aiSunoSyncJob")) schedules.put(name,"0 * * * * *");
        for(String name:List.of("tradeOrderAutoReceiveJob","tradeOrderAutoCommentJob","brokerageRecordUnfreezeJob","customerAutoPutPoolJob")) schedules.put(name,"0 */5 * * * *");
        for(String name:List.of("productStatisticsJob","tradeStatisticsJob")) schedules.put(name,"0 10 2 * * *");
        schedules.putAll(Binder.get(environment).bind("aicloud.job.local-schedules",Bindable.mapOf(String.class,String.class)).orElse(Map.of()));
        String producer=environment.getRequiredProperty("spring.application.name");
        int count=0;
        for(String name:context.getBeanNamesForAnnotation(Component.class)) {
            Class<?> type=context.getType(name);
            if(type==null || !type.getName().contains(".job.")) continue;
            Object bean=context.getBean(name);
            var methods=MethodIntrospector.selectMethods(AopUtils.getTargetClass(bean),(MethodIntrospector.MetadataLookup<XxlJob>)
                    method->AnnotatedElementUtils.findMergedAnnotation(method,XxlJob.class));
            for(var entry:methods.entrySet()) {
                String handler=entry.getValue().value(), cron=schedules.get(handler);
                if(cron==null || "-".equals(cron)) continue;
                Method method=AopUtils.selectInvocableMethod(entry.getKey(),bean.getClass());
                if(method.getParameterCount()!=0) throw new IllegalArgumentException("本地任务必须无参数："+handler);
                var trigger = new CronTrigger(cron,ZoneId.of("Asia/Shanghai"));
                var scheduledAt = new AtomicReference<Instant>();
                scheduler.schedule(()->execute(producer,handler,bean,method,scheduledAt.get()), triggerContext -> {
                    Instant next = trigger.nextExecution(triggerContext);
                    scheduledAt.set(next);
                    return next;
                });
                count++;
            }
        }
        log.info("Local business scheduler ready: application={}, jobs={}, distributedLock=Redis",producer,count);
    }
    void execute(String producer,String handler,Object bean,Method method,Instant scheduledAt) {
        var lock=redis.getLock("aicloud:local-job:"+producer+":"+handler);
        XxlJobContext previous=XxlJobContext.getXxlJobContext();
        try {
            if(!lock.tryLock()) return; // Redisson watchdog retains the lease while this process is alive.
            // A lock alone still permits a second instance to repeat a fast task after the first unlocks.
            org.redisson.api.RBucket<Long> completed = redis.getBucket("aicloud:local-job-completed:"+producer+":"+handler);
            Long previousRun = completed.get();
            if (previousRun != null && previousRun >= scheduledAt.toEpochMilli()) return;
            XxlJobContext job=new XxlJobContext(0,"",null,0,1);
            XxlJobContext.setXxlJobContext(job);
            method.invoke(bean); // Invoke the proxy, preserving @TenantJob and transactional advice.
            if(job.getHandleCode()!=XxlJobContext.HANDLE_CODE_SUCCESS) log.warn("Local business job failed: application={}, handler={}",producer,handler);
            else completed.set(scheduledAt.toEpochMilli(),Duration.ofDays(7));
        } catch(Exception failure) {
            log.warn("Local business job pending retry: application={}, handler={}, failure={}",producer,handler,failure.getClass().getSimpleName());
        } finally {
            XxlJobContext.setXxlJobContext(previous);
            try { if(lock.isHeldByCurrentThread()) lock.unlock(); }
            catch (Exception failure) { log.warn("Local job lock cleanup failed: application={}, handler={}",producer,handler); }
        }
    }
    @Override public void close() { scheduler.shutdown(); }
}
