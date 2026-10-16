package com.aicloud.module.oa.service.leave;

import com.aicloud.framework.common.util.json.JsonUtils;
import com.aicloud.framework.mybatis.core.integration.*;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.framework.tenant.core.util.TenantUtils;
import com.aicloud.module.hrm.api.business.HrmOaApi;
import com.aicloud.module.oa.dal.dataobject.leave.OaLeaveApplyDO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OaHrmIntegration implements IntegrationEventHandler {
    @Resource private HrmOaApi hrm;
    @Resource private IntegrationOutboxService outbox;
    public void validateEmployee(Long userId) { hrm.employee(userId).checkError(); }
    public void reserve(OaLeaveApplyDO apply,String key) {
        Long tenant=TenantContextHolder.getRequiredTenantId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if(status!=STATUS_COMMITTED) outbox.enqueueIndependent(tenant,eventType(),"hrm-leave-rollback:"+key,
                        new HrmOaApi.Action().setType("LEAVE_ABORT").setKey(key));
            }
        });
        hrm.reserveLeave(leave(apply,key)).checkError();
    }
    public void result(OaLeaveApplyDO apply,boolean approve) {
        if(apply.getIntegrationKey()==null && !approve) return; // Historical rejected applications never held quota.
        String key=apply.getIntegrationKey()==null ? "legacy-oa-leave:"+apply.getId()+":"+apply.getProcessInstanceId() : apply.getIntegrationKey();
        var action=new HrmOaApi.Action().setKey(key).setType(approve ? "LEAVE_APPROVE" : "LEAVE_ABORT").setProcessInstanceId(apply.getProcessInstanceId());
        if(apply.getIntegrationKey()==null) action.setType("LEAVE_IMPORT_APPROVE").setLeave(leave(apply,key));
        enqueue(action);
    }
    public void personnel(String type,String key,Long user,LocalDateTime applyTime,LocalDateTime effectTime,String remark) {
        enqueue(new HrmOaApi.Action().setType(type).setKey(key).setUserId(user).setApplyTime(applyTime).setEffectTime(effectTime).setRemark(remark));
    }
    private void enqueue(HrmOaApi.Action action) {
        outbox.enqueueOrdered(TenantContextHolder.getRequiredTenantId(),eventType(),"hrm-oa:"+action.getType()+":"+action.getKey(),"hrm-oa:"+action.getKey(),action);
    }
    private HrmOaApi.Leave leave(OaLeaveApplyDO apply,String key) {
        return new HrmOaApi.Leave().setKey(key).setSourceId(apply.getId()).setUserId(Long.valueOf(apply.getCreator())).setOaType(apply.getType())
                .setDays(BigDecimal.valueOf(apply.getDays())).setStartTime(apply.getStartTime()).setEndTime(apply.getEndTime()).setReason(apply.getReason());
    }
    @Override public String eventType() { return "HRM_OA_ACTION"; }
    @Override public void handle(IntegrationEvent event) { TenantUtils.execute(event.getTenantId(),()->hrm.execute(JsonUtils.parseObject(event.getPayload(),HrmOaApi.Action.class)).checkError()); }
}
