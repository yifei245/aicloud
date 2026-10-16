package com.aicloud.module.hrm.service.attendance.record;
import com.aicloud.framework.tenant.core.util.TenantUtils;
import com.aicloud.module.hrm.api.business.*;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
public class HrmOaLeaveReconciliation {
    @Resource JdbcTemplate jdbc;
    @Resource OaLeaveDecisionApi origin;
    @Resource HrmOaApiImpl target;
    @Scheduled(initialDelay=60000,fixedDelay=60000)
    public void reconcile() {
        var rows=jdbc.queryForList("SELECT tenant_id,business_key FROM hrm_leave_reservation WHERE source='OA' AND state='RESERVED' AND create_time<DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 5 MINUTE) ORDER BY id LIMIT 100");
        for(var row:rows) try {
            TenantUtils.execute(((Number)row.get("tenant_id")).longValue(),()-> {
                var result=origin.decision((String)row.get("business_key")).getCheckedData();
                if(result!=null) target.execute(result).checkError();
            });
        } catch(Exception failure) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("OA leave reconciliation pending: key={}, failure={}",row.get("business_key"),failure.getClass().getSimpleName());
        }
    }
}
