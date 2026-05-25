package com.aicloud.module.system.biz.controller;

import com.aicloud.module.system.biz.annotation.RequirePermission;
import com.aicloud.module.system.biz.entity.AiDictData;
import com.aicloud.module.system.biz.entity.AiDictType;
import com.aicloud.module.system.biz.model.ApiResponse;
import com.aicloud.module.system.biz.model.dict.DictDataSaveRequest;
import com.aicloud.module.system.biz.model.dict.DictTypeSaveRequest;
import com.aicloud.module.system.biz.service.DictAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统字典")
@RestController
@RequestMapping("/system/dict")
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class DictController {

    private final DictAdminService dictAdminService;

    public DictController(DictAdminService dictAdminService) {
        this.dictAdminService = dictAdminService;
    }

    @Operation(summary = "字典类型列表")
    @RequirePermission("system:dict:query")
    @GetMapping("/type/list")
    public ApiResponse<List<AiDictType>> listTypes(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                                   @RequestParam(name = "keyword", required = false) String keyword,
                                                   @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(dictAdminService.listTypes(parseTenantId(tenantIdHeader), keyword, status));
    }

    @Operation(summary = "保存字典类型")
    @RequirePermission("system:dict:create")
    @PostMapping("/type/save")
    public ApiResponse<AiDictType> saveType(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                            @RequestBody DictTypeSaveRequest request) {
        return ApiResponse.ok(dictAdminService.saveType(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "删除字典类型")
    @RequirePermission("system:dict:delete")
    @DeleteMapping("/type/{id}")
    public ApiResponse<Boolean> deleteType(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                           @PathVariable("id") Long id) {
        dictAdminService.deleteType(parseTenantId(tenantIdHeader), id);
        return ApiResponse.ok(true);
    }

    @Operation(summary = "字典数据列表")
    @RequirePermission("system:dict:query")
    @GetMapping("/data/list")
    public ApiResponse<List<AiDictData>> listData(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                                  @RequestParam(name = "dictType", required = false) String dictType,
                                                  @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(dictAdminService.listData(parseTenantId(tenantIdHeader), dictType, status));
    }

    @Operation(summary = "保存字典数据")
    @RequirePermission("system:dict:update")
    @PostMapping("/data/save")
    public ApiResponse<AiDictData> saveData(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                            @RequestBody DictDataSaveRequest request) {
        return ApiResponse.ok(dictAdminService.saveData(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "删除字典数据")
    @RequirePermission("system:dict:delete")
    @DeleteMapping("/data/{id}")
    public ApiResponse<Boolean> deleteData(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                           @PathVariable("id") Long id) {
        dictAdminService.deleteData(parseTenantId(tenantIdHeader), id);
        return ApiResponse.ok(true);
    }

    private Long parseTenantId(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return 1L;
        }
        return Long.parseLong(tenantIdHeader);
    }
}
