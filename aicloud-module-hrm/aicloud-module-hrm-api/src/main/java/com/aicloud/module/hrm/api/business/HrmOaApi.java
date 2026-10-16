package com.aicloud.module.hrm.api.business;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.hrm.enums.ApiConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@FeignClient(name = ApiConstants.NAME, contextId = "hrmOaApi")
public interface HrmOaApi {
    @GetMapping(ApiConstants.PREFIX + "/oa/employee") CommonResult<Long> employee(@RequestParam("userId") Long userId);
    @PostMapping(ApiConstants.PREFIX + "/oa/reserve-leave") CommonResult<Long> reserveLeave(@RequestBody @Valid Leave request);
    @PostMapping(ApiConstants.PREFIX + "/oa/execute") CommonResult<Boolean> execute(@RequestBody @Valid Action request);
    @Data class Leave {
        @NotBlank @Size(max=160) private String key;
        @NotNull @Positive private Long sourceId;
        @NotNull @Positive private Long userId;
        @NotNull private Integer oaType;
        @NotNull @DecimalMin("0.01") @Digits(integer=4,fraction=2) private BigDecimal days;
        @NotNull private LocalDateTime startTime;
        @NotNull private LocalDateTime endTime;
        @Size(max=500) private String reason;
    }
    @Data class Action {
        @NotBlank @Size(max=160) private String key;
        @NotBlank @Pattern(regexp="LEAVE_APPROVE|LEAVE_IMPORT_APPROVE|LEAVE_ABORT|REGULAR|RESIGN") private String type;
        private Long userId;
        private LocalDateTime applyTime;
        private LocalDateTime effectTime;
        @Size(max=500) private String remark;
        private String processInstanceId;
        @Valid private Leave leave;
    }
}
