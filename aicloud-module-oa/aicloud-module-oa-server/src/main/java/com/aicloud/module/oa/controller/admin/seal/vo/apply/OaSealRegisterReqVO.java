package com.aicloud.module.oa.controller.admin.seal.vo.apply;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "管理后台 - 保管人登记用印或归还")
public class OaSealRegisterReqVO {
    @NotNull(message = "申请编号不能为空")
    @Schema(description = "用印申请编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @PastOrPresent(message = "实际时间不能晚于当前时间")
    @Schema(description = "实际用印/归还时间；不传使用当前时间", type = "integer", format = "int64")
    private LocalDateTime actualTime;
}
