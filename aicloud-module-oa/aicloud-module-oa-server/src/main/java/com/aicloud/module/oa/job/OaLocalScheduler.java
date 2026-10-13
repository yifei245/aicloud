package com.aicloud.module.oa.job;

import com.aicloud.module.oa.job.meetingroom.OaMeetingRoomBookingReminderJob;
import com.aicloud.module.oa.job.schedule.OaScheduleReminderJob;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 本地提醒复用现有租户 Job；采用 XXL-Job 调度时关闭此开关。 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aicloud.oa.local-scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class OaLocalScheduler {

    private final OaMeetingRoomBookingReminderJob meetingRoomJob;
    private final OaScheduleReminderJob scheduleJob;

    @Scheduled(initialDelayString = "${aicloud.oa.reminder-delay-ms:60000}",
            fixedDelayString = "${aicloud.oa.reminder-delay-ms:60000}")
    public void remindMeetings() {
        meetingRoomJob.execute();
    }

    @Scheduled(initialDelayString = "${aicloud.oa.reminder-delay-ms:60000}",
            fixedDelayString = "${aicloud.oa.reminder-delay-ms:60000}")
    public void remindSchedules() {
        scheduleJob.execute();
    }
}
