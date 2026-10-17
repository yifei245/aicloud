package com.aicloud.module.wms.controller.open;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.wms.service.inventory.WmsIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@Validated
@RequestMapping("/open-api/v1/wms/inventory")
@Tag(name="第三方 API - 可用库存")
public class OpenInventoryController {
    @Resource private WmsIntegrationService inventory;
    public record Availability(Long productId,Long warehouseId,String unit,BigDecimal available) {}
    @GetMapping @PreAuthorize("@ss.hasScope('inventory.read')")
    @Operation(summary="按 ERP 商品查询本租户 WMS 可用库存",description="仅应用身份；需要 inventory.read；可用量已扣除预占，不返回预占来源或内部流水")
    public CommonResult<Availability> get(@RequestParam @Positive Long productId,@RequestParam(required=false) @Positive Long warehouseId) {
        var user=com.aicloud.framework.security.core.util.SecurityFrameworkUtils.getLoginUser();
        if(user==null || !com.aicloud.framework.common.enums.UserTypeEnum.APPLICATION.getValue().equals(user.getUserType())) {
            throw new org.springframework.security.access.AccessDeniedException("此接口只接受第三方应用令牌");
        }
        var balance=inventory.balance("ERP",productId,warehouseId);
        if(!balance.isMapped()) throw new com.aicloud.framework.common.exception.ServiceException(404,"商品或仓库不存在");
        return CommonResult.success(new Availability(productId,warehouseId,balance.getUnit(),balance.getAvailableQuantity()));
    }
}
