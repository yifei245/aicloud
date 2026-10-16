package com.aicloud.module.oa.controller.employee;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.security.core.util.SecurityFrameworkUtils;
import com.aicloud.module.oa.controller.admin.reimbursement.OaReimbursementController;
import com.aicloud.module.oa.controller.admin.reimbursement.vo.OaReimbursementRespVO;
import com.aicloud.module.oa.service.reimbursement.OaReimbursementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oa/reimbursement")
@Tag(name="员工端 - 本人reimbursement")
public class EmployeeReimbursementController extends OaReimbursementController {
    @Resource private OaReimbursementService applications;
    @Override
    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('oa:reimbursement:query')")
    public CommonResult<OaReimbursementRespVO> getReimbursement(@RequestParam("id") Long id) {
        var record=applications.getReimbursement(id);
        if(record==null || !java.util.Objects.equals(record.getCreator(),String.valueOf(SecurityFrameworkUtils.getLoginUserId()))) {
            throw new com.aicloud.framework.common.exception.ServiceException(403,"只能查看本人申请");
        }
        return super.getReimbursement(id);
    }
}
