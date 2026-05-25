package com.aicloud.module.merchant.biz.controller;

import com.aicloud.module.merchant.biz.entity.AiMerchantAccount;
import com.aicloud.module.merchant.biz.entity.AiMerchantProfile;
import com.aicloud.module.merchant.biz.model.ApiResponse;
import com.aicloud.module.merchant.biz.model.MerchantAccountSaveRequest;
import com.aicloud.module.merchant.biz.model.MerchantSaveRequest;
import com.aicloud.module.merchant.biz.model.PageResponse;
import com.aicloud.module.merchant.biz.service.MerchantService;
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

@Tag(name = "商家中心")
@RestController
@RequestMapping("/merchant")
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Operation(summary = "商户概览")
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(merchantService.overview(tenantId));
    }

    @Operation(summary = "商户分页")
    @GetMapping("/profile/list")
    public ApiResponse<PageResponse<AiMerchantProfile>> list(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "auditStatus", required = false) String auditStatus,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") long pageSize) {
        return ApiResponse.ok(merchantService.listMerchants(tenantId, keyword, status, auditStatus, pageNo, pageSize));
    }

    @Operation(summary = "商户详情")
    @GetMapping("/profile/{id}")
    public ApiResponse<AiMerchantProfile> get(@PathVariable Long id) {
        return ApiResponse.ok(merchantService.getMerchant(id));
    }

    @Operation(summary = "创建或更新商户")
    @PostMapping({"/profile/create", "/profile/update"})
    public ApiResponse<AiMerchantProfile> save(@Valid @RequestBody MerchantSaveRequest body) {
        return ApiResponse.ok(merchantService.saveMerchant(body));
    }

    @Operation(summary = "商户审核")
    @PostMapping("/profile/audit")
    public ApiResponse<AiMerchantProfile> audit(@RequestParam Long id, @RequestParam String auditStatus) {
        return ApiResponse.ok(merchantService.auditMerchant(id, auditStatus));
    }

    @Operation(summary = "商户启停")
    @PostMapping("/profile/status")
    public ApiResponse<AiMerchantProfile> updateStatus(@RequestParam Long id, @RequestParam String status) {
        return ApiResponse.ok(merchantService.updateMerchantStatus(id, status));
    }

    @Operation(summary = "删除商户")
    @DeleteMapping("/profile/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        merchantService.deleteMerchant(id);
        return ApiResponse.ok(Boolean.TRUE);
    }

    @Operation(summary = "商户账号列表")
    @GetMapping("/account/list")
    public ApiResponse<List<AiMerchantAccount>> listAccounts(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "merchantId", required = false) Long merchantId,
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(merchantService.listAccounts(tenantId, merchantId, status));
    }

    @Operation(summary = "商户账号详情")
    @GetMapping("/account/{id}")
    public ApiResponse<AiMerchantAccount> getAccount(@PathVariable Long id) {
        return ApiResponse.ok(merchantService.getAccount(id));
    }

    @Operation(summary = "创建或更新商户账号")
    @PostMapping({"/account/create", "/account/update"})
    public ApiResponse<AiMerchantAccount> saveAccount(@Valid @RequestBody MerchantAccountSaveRequest body) {
        return ApiResponse.ok(merchantService.saveAccount(body));
    }

    @Operation(summary = "商户账号启停")
    @PostMapping("/account/status")
    public ApiResponse<AiMerchantAccount> updateAccountStatus(@RequestParam Long id, @RequestParam String status) {
        return ApiResponse.ok(merchantService.updateAccountStatus(id, status));
    }

    @Operation(summary = "删除商户账号")
    @DeleteMapping("/account/{id}")
    public ApiResponse<Boolean> deleteAccount(@PathVariable Long id) {
        merchantService.deleteAccount(id);
        return ApiResponse.ok(Boolean.TRUE);
    }
}
