package com.aicloud.module.oa.service.leave;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.framework.common.util.date.LocalDateTimeUtils;
import com.aicloud.framework.common.util.object.BeanUtils;
import com.aicloud.module.bpm.api.task.BpmProcessInstanceApi;
import com.aicloud.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import com.aicloud.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.aicloud.module.oa.controller.admin.leave.vo.OaLeaveApplyPageReqVO;
import com.aicloud.module.oa.controller.admin.leave.vo.OaLeaveApplySaveReqVO;
import com.aicloud.module.oa.controller.admin.leave.vo.OaLeaveApplySubmitReqVO;
import com.aicloud.module.oa.dal.dataobject.leave.OaLeaveApplyDO;
import com.aicloud.module.oa.dal.mysql.leave.OaLeaveApplyMapper;
import com.aicloud.module.oa.enums.BpmModelConstants;
import com.aicloud.module.oa.enums.leave.OaLeaveTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import com.aicloud.module.oa.service.attendance.OaAttendanceService;
import com.aicloud.module.oa.enums.attendance.OaAttendanceTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.aicloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.aicloud.module.oa.enums.ErrorCodeConstants.*;

/**
 * 请假申请 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaLeaveApplyServiceImpl implements OaLeaveApplyService {

    @Resource
    @Lazy // 延迟，避免循环依赖报错
    private OaAttendanceService attendanceService;
    @Resource private OaHrmIntegration hrmIntegration;
    @Resource private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Resource
    private OaLeaveApplyMapper leaveApplyMapper;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLeaveApply(OaLeaveApplySaveReqVO saveReqVO) {
        // 1. 计算并校验单次请假天数
        Integer days = LocalDateTimeUtils.getDaysBetweenCeiling(saveReqVO.getStartTime(), saveReqVO.getEndTime());
        validateLeaveApplyDays(saveReqVO.getType(), days);

        // 2. 转换请假内容，保存草稿
        OaLeaveApplyDO leaveApply = BeanUtils.toBean(saveReqVO, OaLeaveApplyDO.class)
                .setId(null).setStatus(BpmProcessInstanceStatusEnum.NOT_START.getStatus())
                .setDays(days);
        leaveApplyMapper.insert(leaveApply);
        return leaveApply.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLeaveApply(OaLeaveApplySaveReqVO saveReqVO, Long userId) {
        // 1.1 校验申请可修改
        validateLeaveApplyEditable(saveReqVO.getId(), userId);
        // 1.2 计算并校验单次请假天数
        Integer days = LocalDateTimeUtils.getDaysBetweenCeiling(saveReqVO.getStartTime(), saveReqVO.getEndTime());
        validateLeaveApplyDays(saveReqVO.getType(), days);

        // 2. 更新请假申请
        OaLeaveApplyDO leaveApply = BeanUtils.toBean(saveReqVO, OaLeaveApplyDO.class)
                .setDays(days);
        leaveApplyMapper.updateById(leaveApply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitLeaveApply(OaLeaveApplySubmitReqVO submitReqVO, Long userId) {
        jdbc.queryForList("SELECT id FROM oa_leave_apply WHERE id=? AND tenant_id=? AND deleted=0 FOR UPDATE",submitReqVO.getId(),com.aicloud.framework.tenant.core.context.TenantContextHolder.getRequiredTenantId());
        // 1.1 校验申请可提交
        OaLeaveApplyDO leaveApply = validateLeaveApplyEditable(submitReqVO.getId(), userId);
        // 1.2 校验历史草稿的单次请假天数
        validateLeaveApplyDays(leaveApply.getType(), leaveApply.getDays());

        // 2. 更新为审批中
        String reservationKey="oa-leave:"+leaveApply.getId()+":"+java.util.UUID.randomUUID();
        leaveApplyMapper.updateById(new OaLeaveApplyDO().setId(leaveApply.getId()).setIntegrationKey(reservationKey)
                .setStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus()));
        hrmIntegration.reserve(leaveApply,reservationKey);

        // 3.1 发起审批
        Map<String, Object> variables = new HashMap<>();
        variables.put("days", leaveApply.getDays());
        String processInstanceId = processInstanceApi.createProcessInstance(userId,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey(BpmModelConstants.LEAVE_APPLY)
                        .setBusinessKey(leaveApply.getId().toString()).setVariables(variables)
                        .setStartUserSelectAssignees(submitReqVO.getStartUserSelectAssignees())).getCheckedData();
        // 3.2 绑定流程编号
        leaveApplyMapper.updateById(new OaLeaveApplyDO().setId(leaveApply.getId()).setProcessInstanceId(processInstanceId));
    }

    @Override
    public OaLeaveApplyDO getLeaveApply(Long id) {
        return leaveApplyMapper.selectById(id);
    }

    @Override
    public PageResult<OaLeaveApplyDO> getLeaveApplyPage(Long userId, OaLeaveApplyPageReqVO pageReqVO) {
        return leaveApplyMapper.selectPage(userId, pageReqVO);
    }

    @Override
    public Map<Long, Integer> getApprovedLeaveDaysMap(Collection<Long> userIds, LocalDateTime[] startTime) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return leaveApplyMapper.selectDaysMapByCreatorsAndStatusAndStartTime(
                userIds, BpmProcessInstanceStatusEnum.APPROVE.getStatus(), startTime);
    }

    /**
     * 校验单次请假天数，不校验年度累计额度
     *
     * @param type 请假类型
     * @param days 请假天数
     */
    private void validateLeaveApplyDays(Integer type, Integer days) {
        OaLeaveTypeEnum leaveType = OaLeaveTypeEnum.valueOf(type);
        if (leaveType != null && leaveType.getMaxDays() != null && days > leaveType.getMaxDays()) {
            throw exception(LEAVE_APPLY_DAYS_EXCEEDED, leaveType.getName(), leaveType.getMaxDays());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLeaveApplyStatus(Long id, Integer status) {
        // 1. 校验申请存在
        OaLeaveApplyDO apply = validateLeaveApplyExists(id);

        if (ObjUtil.equal(apply.getStatus(), status)) {
            return;
        }

        // 2. 更新审批状态
        leaveApplyMapper.updateById(new OaLeaveApplyDO().setId(id).setStatus(status));

        // HRM owns leave balances and attendance; OA retains the application only.
        if (BpmProcessInstanceStatusEnum.isProcessEndStatus(status)) hrmIntegration.result(apply,
                ObjUtil.equal(status,BpmProcessInstanceStatusEnum.APPROVE.getStatus()));
    }

    /**
     * 校验申请可修改或提交
     *
     * @param id 申请编号
     * @param userId 当前用户编号
     * @return 本人的未提交申请
     */
    private OaLeaveApplyDO validateLeaveApplyEditable(Long id, Long userId) {
        // 1. 查询申请
        OaLeaveApplyDO leaveApply = leaveApplyMapper.selectById(id);
        if (leaveApply == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
        // 2. 校验本人归属
        if (ObjUtil.notEqual(leaveApply.getCreator(), userId.toString())) {
            throw exception(APPLY_ACCESS_DENIED);
        }
        // 3. 只有未提交草稿允许修改或提交
        if (ObjUtil.notEqual(leaveApply.getStatus(), BpmProcessInstanceStatusEnum.NOT_START.getStatus())) {
            throw exception(APPLY_STATUS_INVALID);
        }
        return leaveApply;
    }

    /**
     * 校验请假申请存在
     *
     * @param id 申请编号
     * @return 申请
     */
    @SuppressWarnings("UnusedReturnValue")
    private OaLeaveApplyDO validateLeaveApplyExists(Long id) {
        OaLeaveApplyDO leaveApply = leaveApplyMapper.selectById(id);
        if (leaveApply == null) {
            throw exception(APPLY_NOT_EXISTS);
        }
        return leaveApply;
    }

}
