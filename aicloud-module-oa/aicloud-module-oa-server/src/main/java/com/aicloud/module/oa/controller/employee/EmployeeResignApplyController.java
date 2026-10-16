package com.aicloud.module.oa.controller.employee;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.security.core.util.SecurityFrameworkUtils;
import com.aicloud.module.oa.controller.admin.resign.OaResignApplyController;
import com.aicloud.module.oa.controller.admin.resign.vo.OaResignApplyRespVO;
import com.aicloud.module.oa.service.resign.OaResignApplyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oa/resign-apply")
@Tag(name="员工端 - 本人resign-apply")
public class EmployeeResignApplyController extends OaResignApplyController {
    @Resource private OaResignApplyService applications;
    @Override
    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('oa:resign-apply:query')")
    public CommonResult<OaResignApplyRespVO> getResignApply(@RequestParam("id") Long id) {
        var record=applications.getResignApply(id);
        if(record==null || !java.util.Objects.equals(record.getCreator(),String.valueOf(SecurityFrameworkUtils.getLoginUserId()))) {
            throw new com.aicloud.framework.common.exception.ServiceException(403,"只能查看本人申请");
        }
        return super.getResignApply(id);
    }
}
