package com.aicloud.module.product.biz.controller;

import com.aicloud.module.product.biz.model.ApiResponse;
import com.aicloud.module.product.biz.service.ProductExtensionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PRODUCT 扩展管理")
@RestController
@RequestMapping("/product")
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class ProductExtensionController {

    private final ProductExtensionService extensionService;

    public ProductExtensionController(ProductExtensionService extensionService) {
        this.extensionService = extensionService;
    }

    @Operation(summary = "扩展资源列表")
    @GetMapping("/{resource}/list")
    public ApiResponse<Map<String, Object>> list(@PathVariable("resource") String resource,
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResponse.ok(extensionService.list(resource, tenantId, keyword));
    }

    @Operation(summary = "扩展资源详情")
    @GetMapping("/{resource}/{id}")
    public ApiResponse<Object> get(@PathVariable("resource") String resource,
            @PathVariable("id") Long id,
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(extensionService.get(resource, id, tenantId));
    }

    @Operation(summary = "扩展资源保存")
    @PostMapping({"/{resource}/save", "/{resource}/create", "/{resource}/update"})
    public ApiResponse<Object> save(@PathVariable("resource") String resource,
            @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(extensionService.save(resource, body));
    }

    @Operation(summary = "扩展资源状态变更")
    @PostMapping("/{resource}/status")
    public ApiResponse<Boolean> status(@PathVariable("resource") String resource,
            @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(extensionService.updateStatus(resource, body));
    }

    @Operation(summary = "扩展资源删除")
    @DeleteMapping("/{resource}/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("resource") String resource,
            @PathVariable("id") Long id,
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(extensionService.delete(resource, id, tenantId));
    }
}
