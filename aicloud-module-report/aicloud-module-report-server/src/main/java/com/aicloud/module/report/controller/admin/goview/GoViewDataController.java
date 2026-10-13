package com.aicloud.module.report.controller.admin.goview;

import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.report.controller.admin.goview.vo.data.GoViewDataGetBySqlReqVO;
import com.aicloud.module.report.controller.admin.goview.vo.data.GoViewDataRespVO;
import com.aicloud.module.report.service.goview.GoViewDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.util.*;

import static com.aicloud.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - GoView 数据", description = "提供 MySQL 查询和真实 API 访问统计")
@RestController
@RequestMapping("/report/go-view/data")
@Validated
public class GoViewDataController {

    @Resource
    private GoViewDataService goViewDataService;

    @RequestMapping("/get-by-sql")
    @Operation(summary = "使用 SQL 查询数据")
    @PreAuthorize("@ss.hasPermission('report:go-view-data:get-by-sql')")
    public CommonResult<GoViewDataRespVO> getDataBySQL(@Valid @RequestBody GoViewDataGetBySqlReqVO reqVO) {
        return success(goViewDataService.getDataBySQL(reqVO.getSql()));
    }

    @RequestMapping("/get-by-http")
    @Operation(summary = "查询最近 30 天 API 访问统计")
    @PreAuthorize("@ss.hasPermission('report:go-view-data:get-by-http')")
    public CommonResult<GoViewDataRespVO> getDataByHttp(
            @RequestParam(required = false) Map<String, String> params,
            @RequestBody(required = false) String body) { // params、body 按照需要去接收，这里仅仅是示例
        return success(goViewDataService.getDataBySQL("SELECT DATE(begin_time) AS date, COUNT(*) AS requests, "
                + "COUNT(DISTINCT user_id) AS users FROM infra_api_access_log WHERE tenant_id = "
                + TenantContextHolder.getRequiredTenantId()
                + " AND begin_time >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) GROUP BY DATE(begin_time) ORDER BY date"));
    }

}
