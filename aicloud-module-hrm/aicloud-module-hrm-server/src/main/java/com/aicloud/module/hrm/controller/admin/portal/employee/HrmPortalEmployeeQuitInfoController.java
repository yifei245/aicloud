package com.aicloud.module.hrm.controller.admin.portal.employee;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.common.util.object.BeanUtils;
import com.aicloud.module.hrm.controller.admin.employee.vo.quitinfo.HrmEmployeeQuitInfoRespVO;
import com.aicloud.module.hrm.dal.dataobject.employee.employment.HrmEmployeeQuitInfoDO;
import com.aicloud.module.hrm.service.employee.employment.HrmEmployeeQuitInfoService;
import com.aicloud.module.hrm.service.employee.info.HrmEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.aicloud.framework.common.pojo.CommonResult.success;
import static com.aicloud.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - HRM 员工端离职信息")
@RestController
@RequestMapping("/hrm/portal/employee/quit-info")
@Validated
public class HrmPortalEmployeeQuitInfoController {

    @Resource
    private HrmEmployeeQuitInfoService quitInfoService;
    @Resource
    private HrmEmployeeService employeeService;

    @GetMapping("/get")
    @Operation(summary = "获得我的离职信息")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<HrmEmployeeQuitInfoRespVO> getQuitInfo() {
        Long employeeId = employeeService.validateEmployeeBySelf(getLoginUserId()).getId();
        HrmEmployeeQuitInfoDO quitInfo = quitInfoService.getQuitInfoByEmployeeId(employeeId);
        return success(BeanUtils.toBean(quitInfo, HrmEmployeeQuitInfoRespVO.class));
    }

}
