package com.aicloud.module.oa.api.leave;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.aicloud.module.hrm.api.business.*;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class OaLeaveDecisionApiImpl implements OaLeaveDecisionApi {
    @Resource private JdbcTemplate jdbc;
    @Override @Transactional(rollbackFor=Exception.class)
    public CommonResult<HrmOaApi.Action> decision(String key) {
        var rows=jdbc.queryForList("SELECT status,process_instance_id FROM oa_leave_apply WHERE tenant_id=? AND integration_key=? AND deleted=0 FOR UPDATE",TenantContextHolder.getRequiredTenantId(),key);
        var action=new HrmOaApi.Action().setKey(key).setType("LEAVE_ABORT");
        if(rows.isEmpty()) return CommonResult.success(action);
        var row=rows.getFirst(); int state=((Number)row.get("status")).intValue();
        if(state==BpmProcessInstanceStatusEnum.RUNNING.getStatus()) return CommonResult.success(null);
        action.setProcessInstanceId((String)row.get("process_instance_id"));
        if(state==BpmProcessInstanceStatusEnum.APPROVE.getStatus()) action.setType("LEAVE_APPROVE");
        return CommonResult.success(action);
    }
}
