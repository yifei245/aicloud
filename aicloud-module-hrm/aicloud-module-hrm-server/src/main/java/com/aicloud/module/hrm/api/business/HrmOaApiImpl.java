package com.aicloud.module.hrm.api.business;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.mybatis.core.integration.IntegrationInboxService;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeRegularReqVO;
import com.aicloud.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeQuitReqVO;
import com.aicloud.module.hrm.service.attendance.record.HrmLeaveBalanceService;
import com.aicloud.module.hrm.service.employee.info.HrmEmployeeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HrmOaApiImpl implements HrmOaApi {
    @Resource private HrmEmployeeService employees;
    @Resource private HrmLeaveBalanceService balances;
    @Resource private IntegrationInboxService inbox;
    @Override public CommonResult<Long> employee(Long userId) { return CommonResult.success(employees.validateEmployeeBySelf(userId).getId()); }
    @Override public CommonResult<Long> reserveLeave(Leave request) { return CommonResult.success(balances.reserveOa(request)); }
    @Override public CommonResult<Boolean> execute(Action action) {
        Boolean result=inbox.execute(TenantContextHolder.getRequiredTenantId(),"hrm-oa-action",action.getType()+":"+action.getKey(),action,Boolean.class,()-> {
            switch(action.getType()) {
                case "LEAVE_IMPORT_APPROVE" -> {
                    if(action.getLeave()==null) throw new IllegalArgumentException("历史请假信息缺失");
                    balances.reserveOa(action.getLeave()); balances.finish("OA",action.getKey(),true,action.getProcessInstanceId());
                }
                case "LEAVE_APPROVE" -> balances.finish("OA",action.getKey(),true,action.getProcessInstanceId());
                case "LEAVE_ABORT" -> balances.finish("OA",action.getKey(),false,action.getProcessInstanceId());
                case "REGULAR" -> {
                    if(action.getEffectTime()==null) throw new IllegalArgumentException("转正生效时间不能为空");
                    var employee=employees.validateEmployeeBySelf(action.getUserId());
                    employees.regularEmployee(new HrmEmployeeRegularReqVO().setEmployeeId(employee.getId()).setReason(2)
                            .setEffectTime(action.getEffectTime()).setRemark(action.getRemark()));
                }
                case "RESIGN" -> {
                    if(action.getEffectTime()==null || action.getApplyTime()==null) throw new IllegalArgumentException("离职申请及生效时间不能为空");
                    var employee=employees.validateEmployeeBySelf(action.getUserId());
                    employees.quitEmployee(new HrmEmployeeQuitReqVO().setEmployeeId(employee.getId()).setType(1).setReason(10)
                            .setApplyQuitTime(action.getApplyTime()).setPlanQuitTime(action.getEffectTime())
                            .setSalarySettlementTime(action.getEffectTime()).setRemark(action.getRemark()));
                }
                default -> throw new IllegalArgumentException("未知 OA 人事动作");
            }
            return true;
        });
        return CommonResult.success(result);
    }
}
