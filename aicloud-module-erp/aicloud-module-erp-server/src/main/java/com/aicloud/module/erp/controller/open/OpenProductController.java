package com.aicloud.module.erp.controller.open;

import com.aicloud.framework.common.enums.CommonStatusEnum;
import com.aicloud.framework.common.pojo.*;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.erp.dal.dataobject.product.ErpProductDO;
import com.aicloud.module.erp.dal.mysql.product.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@Validated
@RequestMapping("/open-api/v1/erp/products")
@Tag(name="第三方 API - 商品目录")
public class OpenProductController {
    @Resource private ErpProductMapper products;
    @Resource private ErpProductUnitMapper units;
    public record Product(Long id,String name,String barCode,String unit,BigDecimal salePrice) {}
    @Data public static class Query extends PageParam {
        @Size(max=100) private String name;
    }
    @GetMapping @PreAuthorize("@ss.hasScope('catalog.read')")
    @Operation(summary="分页查询本租户启用的商品目录",description="仅应用身份；需要 catalog.read，不返回采购价或内部备注")
    public CommonResult<PageResult<Product>> page(@Valid Query query) {
        requireApplication();
        var page=products.selectPage(query,new LambdaQueryWrapperX<ErpProductDO>()
                .eq(ErpProductDO::getStatus,CommonStatusEnum.ENABLE.getStatus()).likeIfPresent(ErpProductDO::getName,query.getName()).orderByDesc(ErpProductDO::getId));
        return CommonResult.success(new PageResult<>(page.getList().stream().map(this::view).toList(),page.getTotal()));
    }
    @GetMapping("/{id}") @PreAuthorize("@ss.hasScope('catalog.read')")
    @Operation(summary="查询本租户启用商品的公开字段")
    public CommonResult<Product> get(@PathVariable @Positive Long id) {
        requireApplication();
        var product=products.selectById(id);
        if(product==null || !CommonStatusEnum.isEnable(product.getStatus())) throw new com.aicloud.framework.common.exception.ServiceException(404,"商品不存在");
        return CommonResult.success(view(product));
    }
    private Product view(ErpProductDO product) {
        var unit=units.selectById(product.getUnitId());
        return new Product(product.getId(),product.getName(),product.getBarCode(),unit==null ? null : unit.getName(),product.getSalePrice());
    }
    private void requireApplication() {
        var user=com.aicloud.framework.security.core.util.SecurityFrameworkUtils.getLoginUser();
        if(user==null || !com.aicloud.framework.common.enums.UserTypeEnum.APPLICATION.getValue().equals(user.getUserType())) {
            throw new org.springframework.security.access.AccessDeniedException("此接口只接受第三方应用令牌");
        }
    }
}
