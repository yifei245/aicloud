package com.aicloud.module.erp.biz.controller;

import com.aicloud.module.erp.biz.entity.AiErpInventory;
import com.aicloud.module.erp.biz.entity.AiErpStockCheck;
import com.aicloud.module.erp.biz.entity.AiErpStockRecord;
import com.aicloud.module.erp.biz.model.ApiResponse;
import com.aicloud.module.erp.biz.model.ErpInventoryAdjustRequest;
import com.aicloud.module.erp.biz.model.ErpStockCheckSaveRequest;
import com.aicloud.module.erp.biz.service.ErpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ERP 库存供应链")
@RestController
@RequestMapping("/erp")
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class ErpController {

    private final ErpService erpService;

    public ErpController(ErpService erpService) {
        this.erpService = erpService;
    }

    @Operation(summary = "库存列表")
    @GetMapping("/inventory/list")
    public ApiResponse<List<AiErpInventory>> inventoryList(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "warehouseCode", required = false) String warehouseCode,
            @RequestParam(name = "skuCode", required = false) String skuCode) {
        return ApiResponse.ok(erpService.inventoryList(tenantId, warehouseCode, skuCode));
    }

    @Operation(summary = "库存详情")
    @GetMapping("/inventory/{id}")
    public ApiResponse<AiErpInventory> inventoryDetail(@PathVariable Long id) {
        return ApiResponse.ok(erpService.getInventory(id));
    }

    @Operation(summary = "库存汇总")
    @GetMapping("/inventory/summary")
    public ApiResponse<Map<String, Object>> summary(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "warehouseCode", required = false) String warehouseCode) {
        return ApiResponse.ok(erpService.summary(tenantId, warehouseCode));
    }

    @Operation(summary = "低库存预警")
    @GetMapping("/inventory/low-stock")
    public ApiResponse<List<AiErpInventory>> lowStock(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "warehouseCode", required = false) String warehouseCode) {
        return ApiResponse.ok(erpService.lowStock(tenantId, warehouseCode));
    }

    @Operation(summary = "库存调整")
    @PostMapping("/inventory/adjust")
    public ApiResponse<AiErpInventory> adjust(@Valid @RequestBody ErpInventoryAdjustRequest body) {
        return ApiResponse.ok(erpService.adjust(body));
    }

    @Operation(summary = "锁定库存")
    @PostMapping("/inventory/lock")
    public ApiResponse<AiErpInventory> lock(
            @RequestParam Long tenantId,
            @RequestParam String skuCode,
            @RequestParam String warehouseCode,
            @RequestParam Integer qty,
            @RequestParam(required = false) String bizNo,
            @RequestParam(required = false) String createBy) {
        return ApiResponse.ok(erpService.lock(tenantId, skuCode, warehouseCode, qty, bizNo, createBy));
    }

    @Operation(summary = "释放库存")
    @PostMapping("/inventory/release")
    public ApiResponse<AiErpInventory> release(
            @RequestParam Long tenantId,
            @RequestParam String skuCode,
            @RequestParam String warehouseCode,
            @RequestParam Integer qty,
            @RequestParam(required = false) String bizNo,
            @RequestParam(required = false) String createBy) {
        return ApiResponse.ok(erpService.release(tenantId, skuCode, warehouseCode, qty, bizNo, createBy));
    }

    @Operation(summary = "库存流水列表")
    @GetMapping("/stock-record/list")
    public ApiResponse<List<AiErpStockRecord>> records(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "skuCode", required = false) String skuCode,
            @RequestParam(name = "bizNo", required = false) String bizNo) {
        return ApiResponse.ok(erpService.recordList(tenantId, skuCode, bizNo));
    }

    @Operation(summary = "盘点单列表")
    @GetMapping("/stock-check/list")
    public ApiResponse<List<AiErpStockCheck>> checks(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(erpService.checkList(tenantId));
    }

    @Operation(summary = "盘点单详情")
    @GetMapping("/stock-check/{id}")
    public ApiResponse<AiErpStockCheck> checkDetail(@PathVariable Long id) {
        return ApiResponse.ok(erpService.getCheck(id));
    }

    @Operation(summary = "创建或更新盘点单")
    @PostMapping({"/stock-check/create", "/stock-check/update"})
    public ApiResponse<AiErpStockCheck> saveCheck(@Valid @RequestBody ErpStockCheckSaveRequest body) {
        return ApiResponse.ok(erpService.saveCheck(body));
    }

    @Operation(summary = "完成盘点单")
    @PostMapping("/stock-check/finish")
    public ApiResponse<AiErpStockCheck> finishCheck(@RequestParam Long id) {
        return ApiResponse.ok(erpService.finishCheck(id));
    }

    @Operation(summary = "删除盘点单")
    @DeleteMapping("/stock-check/{id}")
    public ApiResponse<Boolean> deleteCheck(@PathVariable Long id) {
        erpService.deleteCheck(id);
        return ApiResponse.ok(Boolean.TRUE);
    }
}
