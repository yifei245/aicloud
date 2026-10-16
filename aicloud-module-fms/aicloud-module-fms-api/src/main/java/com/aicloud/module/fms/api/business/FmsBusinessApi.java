package com.aicloud.module.fms.api.business;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.fms.enums.ApiConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@FeignClient(name = ApiConstants.NAME, contextId = "fmsBusinessApi")
public interface FmsBusinessApi {
    @PostMapping(ApiConstants.PREFIX + "/business/deliver")
    CommonResult<Long> deliver(@RequestBody @Valid Event event);
    @Data
    class Event {
        @NotBlank @Pattern(regexp = "ERP_RECEIPT|ERP_PAYMENT|MALL_PAYMENT|MALL_REFUND|HRM_SALARY|OA_EXPENSE|OA_TRAVEL_EXPENSE") private String source;
        @NotBlank @Pattern(regexp = "CREATE|CANCEL") private String action;
        @NotBlank @Size(max = 160) private String businessKey;
        @NotNull @Positive private Long documentId;
        @NotBlank @Size(max = 100) private String documentNo;
        @NotNull @DecimalMin("0.01") @Digits(integer = 16, fraction = 2) private BigDecimal amount;
        private boolean reverse;
        @NotNull private LocalDateTime occurredAt;
        @Size(max = 300) private String summary;
    }
}
