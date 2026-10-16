package com.aicloud.module.oa.controller.employee;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.security.core.util.SecurityFrameworkUtils;
import com.aicloud.module.oa.controller.admin.leave.OaLeaveApplyController;
import com.aicloud.module.oa.controller.admin.leave.vo.OaLeaveApplyRespVO;
import com.aicloud.module.oa.service.leave.OaLeaveApplyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oa/leave-apply")
@Tag(name="员工端 - 本人leave-apply")
public class EmployeeLeaveApplyController extends OaLeaveApplyController {
    @Resource private OaLeaveApplyService applications;
    @Override
    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('oa:leave-apply:query')")
    public CommonResult<OaLeaveApplyRespVO> getLeaveApply(@RequestParam("id") Long id) {
        var record=applications.getLeaveApply(id);
        if(record==null || !java.util.Objects.equals(record.getCreator(),String.valueOf(SecurityFrameworkUtils.getLoginUserId()))) {
            throw new com.aicloud.framework.common.exception.ServiceException(403,"只能查看本人申请");
        }
        return super.getLeaveApply(id);
    }
}
