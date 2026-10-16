package com.aicloud.module.erp.api.business;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.erp.enums.ApiConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name=ApiConstants.NAME,contextId="erpBusinessApi")
public interface ErpBusinessApi {
    @GetMapping(ApiConstants.PREFIX+"/business/product") CommonResult<Product> product(@RequestParam("id") Long id);
    @GetMapping(ApiConstants.PREFIX+"/business/customer") CommonResult<Boolean> customer(@RequestParam("id") Long id);
    @PostMapping(ApiConstants.PREFIX+"/business/contract-sale") CommonResult<Long> createContractSale(@RequestBody @Valid Contract request);
    @Data class Product {
        private Long id;
        private String name,barCode,unit;
        private Long unitId;
        private boolean enabled;
    }
    @Data class Contract {
        @NotNull @Positive private Long contractId;
        @NotBlank @Size(max=100) private String contractNo;
        @NotNull @Positive private Long customerId;
        @NotNull private LocalDateTime orderTime;
        @NotNull @DecimalMin("0") private BigDecimal totalPrice;
        @NotNull @DecimalMin("0") @DecimalMax("100") private BigDecimal discountPercent;
        @Valid @NotEmpty private List<Item> items;
    }
    @Data class Item {
        @NotNull @Positive private Long productId;
        @NotBlank private String unit;
        @NotNull @DecimalMin("0.00000001") private BigDecimal count;
        @NotNull @DecimalMin("0") private BigDecimal price;
    }
}
