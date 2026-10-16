package com.aicloud.module.oa.controller.employee;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.security.core.util.SecurityFrameworkUtils;
import com.aicloud.module.oa.controller.admin.regular.OaRegularApplyController;
import com.aicloud.module.oa.controller.admin.regular.vo.OaRegularApplyRespVO;
import com.aicloud.module.oa.service.regular.OaRegularApplyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oa/regular-apply")
@Tag(name="员工端 - 本人regular-apply")
public class EmployeeRegularApplyController extends OaRegularApplyController {
    @Resource private OaRegularApplyService applications;
    @Override
    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('oa:regular-apply:query')")
    public CommonResult<OaRegularApplyRespVO> getRegularApply(@RequestParam("id") Long id) {
        var record=applications.getRegularApply(id);
        if(record==null || !java.util.Objects.equals(record.getCreator(),String.valueOf(SecurityFrameworkUtils.getLoginUserId()))) {
            throw new com.aicloud.framework.common.exception.ServiceException(403,"只能查看本人申请");
        }
        return super.getRegularApply(id);
    }
}
