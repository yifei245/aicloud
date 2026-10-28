package com.aicloud.module.wms.api.integration;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.wms.enums.ApiConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import com.aicloud.framework.common.pojo.PageResult;

/** WMS is the physical-stock authority. Source IDs must have explicit mappings. */
@FeignClient(name = ApiConstants.NAME, contextId = "wmsStockApi")
public interface WmsStockApi {
    String PREFIX = ApiConstants.PREFIX + "/integration";

    @PostMapping(PREFIX + "/product")
    CommonResult<Long> syncProduct(@RequestBody @Valid Product request);

    @PostMapping(PREFIX + "/warehouse")
    CommonResult<Long> syncWarehouse(@RequestBody @Valid Warehouse request);

    @PostMapping(PREFIX + "/prepare")
    CommonResult<Balance> prepare(@RequestBody @Valid Movement request);

    @PostMapping(PREFIX + "/commit")
    CommonResult<Boolean> commit(@RequestBody @Valid Command request);

    @PostMapping(PREFIX + "/abort")
    CommonResult<Boolean> abort(@RequestBody @Valid Command request);

    @PostMapping(PREFIX + "/position")
    CommonResult<Boolean> ensurePosition(@RequestBody @Valid Position request);

    @PostMapping(PREFIX + "/positions")
    CommonResult<java.util.List<PositionBalance>> positions(@RequestBody @Size(max = 200) java.util.List<@Positive Long> stockIds);

    @GetMapping(PREFIX + "/balance")
    CommonResult<Balance> balance(@RequestParam("source") String source,
                                  @RequestParam("productId") Long productId,
                                  @RequestParam(value = "warehouseId", required = false) Long warehouseId);

    @GetMapping(PREFIX + "/balances")
    CommonResult<PageResult<SourceBalance>> balances(@RequestParam("source") String source,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "warehouseId", required = false) Long warehouseId,
            @RequestParam(value = "inventoryId", required = false) Long inventoryId,
            @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize);

    @Data
    class Product {
        @NotNull @Positive private Long productId;
        @NotBlank @Size(max = 200) private String name;
        @NotBlank @Size(max = 64) private String unit;
        private String barCode;
        @NotNull private Boolean enabled;
        private BigDecimal purchasePrice;
        private BigDecimal salePrice;
    }

    @Data
    class Warehouse {
        @NotNull @Positive private Long warehouseId;
        @NotBlank @Size(max = 200) private String name;
    }

    @Data
    class Command {
        @Pattern(regexp = "ERP|MES|MALL") @NotBlank private String source;
        @NotBlank @Size(max = 160) private String businessKey;
    }

    @Data
    class Movement extends Command {
        @NotNull @Positive private Long productId;
        @NotNull @Positive private Long warehouseId;
        @NotNull @Digits(integer = 16, fraction = 8) private BigDecimal quantity;
        @NotBlank private String unit;
        @NotNull @Positive private Long documentId;
        @NotBlank @Size(max = 200) private String documentNo;
        private BigDecimal price;
        @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
        @Positive private Long positionId;
        /** Reclassify unallocated stock; does not change warehouse physical quantity. */
        @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
        private Boolean allocationOnly;
    }

    @Data
    class Position {
        @NotNull @Positive private Long stockId;
        @NotNull @Positive private Long productId;
        @NotNull @Positive private Long warehouseId;
        @NotNull @Positive private Long locationId;
        @NotNull @Positive private Long areaId;
        @Positive private Long batchId;
        @Size(max = 128) private String batchCode;
        @NotBlank private String unit;
    }

    @Data
    class PositionBalance {
        private Long stockId;
        private BigDecimal quantity;
        private BigDecimal reservedQuantity;
        private BigDecimal availableQuantity;
    }

    @Data
    class Balance {
        private boolean mapped;
        private String unit;
        private Long skuId;
        private Long warehouseId;
        private BigDecimal quantity;
        private BigDecimal reservedQuantity;
        private BigDecimal availableQuantity;
    }
    @Data
    class SourceBalance extends Balance {
        private Long inventoryId;
        private Long productId;
        private Long sourceWarehouseId;
    }
}
