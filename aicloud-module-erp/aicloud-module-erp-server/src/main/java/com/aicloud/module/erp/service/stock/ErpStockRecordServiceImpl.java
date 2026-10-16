package com.aicloud.module.erp.service.stock;

import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.framework.common.util.object.BeanUtils;
import com.aicloud.module.erp.controller.admin.stock.vo.record.ErpStockRecordPageReqVO;
import com.aicloud.module.erp.dal.dataobject.stock.ErpStockRecordDO;
import com.aicloud.module.erp.dal.mysql.stock.ErpStockRecordMapper;
import com.aicloud.module.erp.service.stock.bo.ErpStockRecordCreateReqBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * ERP 产品库存明细 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpStockRecordServiceImpl implements ErpStockRecordService {

    @Resource
    private ErpStockRecordMapper stockRecordMapper;

    @Resource
    private ErpStockService stockService;
    @Resource private com.aicloud.module.wms.api.integration.WmsStockBridge wmsStock;
    @Resource private com.aicloud.module.erp.service.product.ErpWmsIntegration wmsMaster;

    @Override
    public ErpStockRecordDO getStockRecord(Long id) {
        return stockRecordMapper.selectById(id);
    }

    @Override
    public PageResult<ErpStockRecordDO> getStockRecordPage(ErpStockRecordPageReqVO pageReqVO) {
        return stockRecordMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createStockRecord(ErpStockRecordCreateReqBO createReqBO) {
        // Keep ERP document history; physical availability and changes belong to WMS.
        ErpStockRecordDO stockRecord = BeanUtils.toBean(createReqBO, ErpStockRecordDO.class)
                .setTotalCount(BigDecimal.ZERO);
        stockRecordMapper.insert(stockRecord);
        var product = wmsMaster.ensureProduct(createReqBO.getProductId());
        wmsMaster.ensureWarehouse(createReqBO.getWarehouseId());
        var movement = new com.aicloud.module.wms.api.integration.WmsStockApi.Movement();
        movement.setBusinessKey("stock-record:" + stockRecord.getId());
        movement.setProductId(createReqBO.getProductId()); movement.setWarehouseId(createReqBO.getWarehouseId());
        movement.setQuantity(createReqBO.getCount()); movement.setUnit(product.getUnit());
        movement.setDocumentId(createReqBO.getBizId()); movement.setDocumentNo(createReqBO.getBizNo());
        movement.setPrice(product.getPurchasePrice());
        var balance = wmsStock.prepare(movement, true);
        stockRecordMapper.updateById(new ErpStockRecordDO().setId(stockRecord.getId())
                .setTotalCount(balance.getQuantity().add(createReqBO.getCount())));
    }

}
