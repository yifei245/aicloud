package com.aicloud.module.erp.api.business;

import com.aicloud.framework.common.enums.CommonStatusEnum;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.mybatis.core.integration.IntegrationInboxService;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import com.aicloud.module.erp.service.product.*;
import com.aicloud.module.erp.service.sale.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.*;

@RestController
public class ErpBusinessApiImpl implements ErpBusinessApi {
    @Resource private ErpProductService products;
    @Resource private ErpProductUnitService units;
    @Resource private ErpCustomerService customers;
    @Resource private ErpSaleOrderService sales;
    @Resource private IntegrationInboxService inbox;
    @Override public CommonResult<Product> product(Long id) {
        var product=products.getProduct(id);
        if(product==null) throw new IllegalArgumentException("ERP 商品不存在");
        var unit=units.getProductUnit(product.getUnitId());
        if(unit==null) throw new IllegalArgumentException("ERP 基本单位不存在");
        return CommonResult.success(new Product().setId(id).setName(product.getName()).setBarCode(product.getBarCode())
                .setUnitId(unit.getId()).setUnit(unit.getName()).setEnabled(CommonStatusEnum.isEnable(product.getStatus())));
    }
    @Override public CommonResult<Boolean> customer(Long id) { customers.validateCustomer(id); return CommonResult.success(true); }
    @Override public CommonResult<Long> createContractSale(Contract request) {
        Long order=inbox.execute(TenantContextHolder.getRequiredTenantId(),"crm-contract-sale","contract:"+request.getContractId(),request,Long.class,()-> {
            customers.validateCustomer(request.getCustomerId());
            var items=request.getItems().stream().map(line->{
                var product=product(line.getProductId()).getCheckedData();
                if(!product.isEnabled() || !product.getUnit().equals(line.getUnit())) throw new IllegalArgumentException("ERP 商品已停用或合同计量单位不一致");
                return new ErpSaleOrderSaveReqVO.Item().setProductId(product.getId()).setProductUnitId(product.getUnitId())
                        .setProductPrice(line.getPrice()).setCount(line.getCount()).setTaxPercent(BigDecimal.ZERO)
                        .setRemark("CRM 合同 "+request.getContractNo());
            }).toList();
            Long id=sales.createSaleOrder(new ErpSaleOrderSaveReqVO().setCustomerId(request.getCustomerId()).setOrderTime(request.getOrderTime())
                    .setDiscountPercent(request.getDiscountPercent()).setDepositPrice(BigDecimal.ZERO).setItems(items)
                    .setRemark("来源 CRM 合同 "+request.getContractNo()+" (#"+request.getContractId()+")；金额未拆税，税务参数须在 ERP 审核前确认"));
            if(sales.getSaleOrder(id).getTotalPrice().compareTo(request.getTotalPrice())!=0) throw new IllegalArgumentException("合同与 ERP 销售单金额不一致，已回滚创建");
            return id;
        });
        return CommonResult.success(order);
    }
}
