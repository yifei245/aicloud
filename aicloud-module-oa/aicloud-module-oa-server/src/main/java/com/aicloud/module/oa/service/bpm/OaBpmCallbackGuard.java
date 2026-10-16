package com.aicloud.module.oa.service.bpm;

import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.aicloud.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

/** Bind durable callbacks to the committed submission before applying any business effect. */
@Service
public class OaBpmCallbackGuard {
    private static final Set<String> TABLES = Set.of("oa_reimbursement", "oa_overtime_apply", "oa_leave_apply",
            "oa_regular_apply", "oa_resign_apply", "oa_supply_apply", "oa_travel_apply", "oa_travel_reimbursement",
            "oa_seal_apply", "oa_vehicle_apply", "oa_vehicle_return", "oa_meeting_room_booking", "oa_official_doc_send", "oa_official_doc_receive");
    @Resource private JdbcTemplate jdbc;
    @Transactional(rollbackFor = Exception.class)
    public void execute(String expectedKey, BpmProcessInstanceStatusEvent event, Runnable action) {
        if (!TABLES.contains(expectedKey) || !expectedKey.equals(event.getProcessDefinitionKey())
                || event.getId() == null || !BpmProcessInstanceStatusEnum.isProcessEndStatus(event.getStatus())) {
            throw new IllegalArgumentException("审批回调的流程类型、实例或结果无效");
        }
        // Names come exclusively from the fixed allowlist; all data predicates are parameterized.
        var rows = jdbc.queryForList("SELECT process_instance_id,status FROM " + expectedKey
                + " WHERE id=? AND tenant_id=? AND deleted=0 FOR UPDATE", Long.valueOf(event.getBusinessKey()), TenantContextHolder.getRequiredTenantId());
        if (rows.isEmpty()) return;
        var row = rows.getFirst();
        if (!event.getId().equals(row.get("process_instance_id"))) return; // Rolled-back or superseded submission.
        if (((Number) row.get("status")).intValue() != BpmProcessInstanceStatusEnum.RUNNING.getStatus()) return;
        action.run();
    }
}
