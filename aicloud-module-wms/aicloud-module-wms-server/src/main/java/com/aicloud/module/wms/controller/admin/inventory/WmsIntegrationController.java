package com.aicloud.module.wms.controller.admin.inventory;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.wms.service.inventory.WmsIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/wms/integration")
@Tag(name = "管理后台 - WMS 业务映射")
public class WmsIntegrationController {
    @Resource private WmsIntegrationService service;
    @PostMapping("/bind")
    @Operation(summary = "关联渠道商品、生产物料或业务仓库至 WMS")
    @PreAuthorize("@ss.hasPermission('wms:inventory:query') && @ss.hasPermission('wms:item:update')")
    public CommonResult<Boolean> bind(@RequestBody @Valid Binding request) {
        service.bind(request.getKind(), request.getSource(), request.getSourceId(), request.getTargetId());
        return CommonResult.success(true);
    }
    @GetMapping("/mappings")
    @Operation(summary = "查询本租户跨模块映射")
    @PreAuthorize("@ss.hasPermission('wms:inventory:query')")
    public CommonResult<List<Map<String, Object>>> mappings(@RequestParam String kind, @RequestParam String source) {
        return CommonResult.success(service.mappings(kind, source));
    }
    @Data public static class Binding {
        @NotBlank @Pattern(regexp = "PRODUCT|WAREHOUSE") private String kind;
        @NotBlank @Pattern(regexp = "MES|MALL|ERP") private String source;
        @NotNull @Positive private Long sourceId;
        @NotNull @Positive private Long targetId;
    }
}
