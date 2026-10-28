package com.aicloud.module.mes.controller.admin.wm.materialstock.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "MES - 将 WMS 未分配库存上架到生产批次和库位，不增加实物总量")
public class MesWmMaterialStockAllocateReqVO {
    @NotBlank @Size(max = 100) private String businessKey;
    @NotNull @Positive private Long itemId;
    @NotNull @Positive private Long warehouseId;
    @NotNull @Positive private Long locationId;
    @NotNull @Positive private Long areaId;
    @Positive private Long batchId;
    @jakarta.validation.Valid
    private com.aicloud.module.mes.controller.admin.wm.batch.vo.MesWmBatchGenerateReqVO batch;
    @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 16, fraction = 8) private BigDecimal quantity;
}
