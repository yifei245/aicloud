package com.aicloud.module.crm.service.contract;

import com.aicloud.framework.common.util.json.JsonUtils;
import com.aicloud.framework.mybatis.core.integration.*;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.framework.tenant.core.util.TenantUtils;
import com.aicloud.module.erp.api.business.ErpBusinessApi;
import com.aicloud.module.crm.dal.dataobject.contract.CrmContractDO;
import com.aicloud.module.crm.dal.mysql.contract.CrmContractProductMapper;
import com.aicloud.module.crm.dal.mysql.product.CrmProductMapper;
import com.aicloud.module.crm.service.customer.CrmCustomerService;
import com.aicloud.module.crm.service.product.CrmProductService;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class CrmErpIntegration implements IntegrationEventHandler {
    @Resource private JdbcTemplate jdbc;
    @Resource private CrmContractProductMapper lines;
    @Resource private CrmProductMapper products;
    @Resource private CrmProductService productService;
    @Resource private CrmCustomerService customerService;
    @Resource private ErpBusinessApi erp;
    @Resource private IntegrationOutboxService outbox;
    @Transactional(rollbackFor=Exception.class)
    public void bind(String kind,Long crmId,Long erpId,String unit) {
        Integer sourceUnit=null;
        if("PRODUCT".equals(kind)) {
            var local=productService.getProduct(crmId);
            if(local==null) throw new IllegalArgumentException("CRM 商品不存在");
            var master=erp.product(erpId).getCheckedData();
            if(!master.isEnabled() || !Objects.equals(master.getUnit(),unit)) throw new IllegalArgumentException("请明确确认与 ERP 主档相同的基本计量单位");
            sourceUnit=local.getUnit();
        } else if("CUSTOMER".equals(kind)) {
            if(customerService.getCustomer(crmId)==null) throw new IllegalArgumentException("CRM 客户不存在");
            erp.customer(erpId).checkError();
        } else throw new IllegalArgumentException("映射类型无效");
        jdbc.update("INSERT INTO crm_erp_mapping(tenant_id,kind,crm_id,erp_id,source_unit,unit) VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE id=id",
                tenant(),kind,crmId,erpId,sourceUnit,unit);
        var saved=mapping(kind,crmId);
        if(((Number)saved.get("erp_id")).longValue()!=erpId || !Objects.equals(saved.get("unit"),unit)
                || !Objects.equals(saved.get("source_unit"),sourceUnit)) throw new IllegalArgumentException("已有业务映射不能直接改绑，请先处理关联单据");
    }
    public ErpBusinessApi.Contract request(CrmContractDO contract) {
        var customer=mapping("CUSTOMER",contract.getCustomerId());
        var details=lines.selectListByContractId(contract.getId());
        if(details.isEmpty()) throw new IllegalArgumentException("自动生成 ERP 销售单必须有商品明细；纯文本合同请先补充明细");
        var items=details.stream().map(line->{
            var mapped=mapping("PRODUCT",line.getProductId());
            var local=products.selectById(line.getProductId());
            if(local==null || local.getUnit()!=((Number)mapped.get("source_unit")).intValue()) throw new IllegalArgumentException("CRM 商品计量单位已改变，请核对商品映射");
            return new ErpBusinessApi.Item().setProductId(((Number)mapped.get("erp_id")).longValue()).setUnit((String)mapped.get("unit"))
                    .setPrice(line.getContractPrice()).setCount(line.getCount());
        }).toList();
        return new ErpBusinessApi.Contract().setContractId(contract.getId()).setContractNo(contract.getNo())
                .setCustomerId(((Number)customer.get("erp_id")).longValue()).setOrderTime(contract.getOrderDate())
                .setDiscountPercent(contract.getDiscountPercent()).setTotalPrice(contract.getTotalPrice()).setItems(items);
    }
    public void approved(CrmContractDO contract) {
        outbox.enqueue(tenant(),eventType(),"crm-contract-sale:"+contract.getId(),request(contract));
    }
    public Map<String,Object> mapping(String kind,Long crmId) {
        var rows=jdbc.queryForList("SELECT erp_id,source_unit,unit FROM crm_erp_mapping WHERE tenant_id=? AND kind=? AND crm_id=?",tenant(),kind,crmId);
        if(rows.isEmpty()) throw new IllegalArgumentException("缺少 CRM 到 ERP 的 "+kind+" 映射："+crmId);
        return rows.getFirst();
    }
    public Long saleOrder(Long contractId) {
        var ids=jdbc.queryForList("SELECT sale_order_id FROM crm_erp_contract_sale WHERE tenant_id=? AND contract_id=?",Long.class,tenant(),contractId);
        return ids.isEmpty() ? null : ids.getFirst();
    }
    @Override public String eventType() { return "CRM_ERP_SALE_ORDER"; }
    @Override public void handle(IntegrationEvent event) {
        TenantUtils.execute(event.getTenantId(),()-> {
            var request=JsonUtils.parseObject(event.getPayload(),ErpBusinessApi.Contract.class);
            Long sale=erp.createContractSale(request).getCheckedData();
            jdbc.update("INSERT INTO crm_erp_contract_sale(tenant_id,contract_id,sale_order_id) VALUES(?,?,?) ON DUPLICATE KEY UPDATE id=id",tenant(),request.getContractId(),sale);
            if(!sale.equals(saleOrder(request.getContractId()))) throw new IllegalStateException("合同销售单映射不一致");
        });
    }
    private static Long tenant() { return TenantContextHolder.getRequiredTenantId(); }
}
