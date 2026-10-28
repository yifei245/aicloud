package com.aicloud.module.mes.service.wm.returnissue;

import com.aicloud.framework.test.core.ut.BaseDbUnitTest;
import com.aicloud.module.mes.controller.admin.wm.returnissue.vo.detail.MesWmReturnIssueDetailSaveReqVO;
import com.aicloud.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import com.aicloud.module.mes.dal.dataobject.wm.returnissue.MesWmReturnIssueLineDO;
import com.aicloud.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import com.aicloud.module.mes.dal.mysql.wm.returnissue.MesWmReturnIssueDetailMapper;
import com.aicloud.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import com.aicloud.module.mes.service.wm.warehouse.*;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@Import(MesWmReturnIssueDetailServiceImpl.class)
class MesWmReturnIssueDetailServiceImplTest extends BaseDbUnitTest {
    @Resource MesWmReturnIssueDetailServiceImpl service;
    @Resource MesWmReturnIssueDetailMapper mapper;
    @MockitoBean MesWmReturnIssueLineService lines;
    @MockitoBean MesWmMaterialStockService stocks;
    @MockitoBean MesWmWarehouseAreaService areas;
    @MockitoBean MesWmWarehouseService warehouses;
    MesWmReturnIssueDetailSaveReqVO request;
    MesWmMaterialStockDO source;
    @BeforeEach void fixture() {
        when(lines.validateReturnIssueLineExists(1L)).thenReturn(new MesWmReturnIssueLineDO().setId(1L).setIssueId(2L)
                .setItemId(3L).setMaterialStockId(4L).setQuantity(BigDecimal.TEN));
        source = new MesWmMaterialStockDO().setId(4L).setItemId(3L).setWarehouseId(100L).setLocationId(101L).setAreaId(102L);
        when(stocks.getMaterialStock(4L)).thenReturn(source);
        when(warehouses.getWarehouseByCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE)).thenReturn(new MesWmWarehouseDO().setId(100L));
        request = new MesWmReturnIssueDetailSaveReqVO().setLineId(1L).setIssueId(2L).setItemId(3L).setMaterialStockId(4L)
                .setWarehouseId(200L).setLocationId(201L).setAreaId(202L).setQuantity(BigDecimal.ONE);
    }
    @Test void returnValidatesLineSideStockAndKeepsIndependentDestination() {
        Long id = service.createReturnIssueDetail(request);
        verify(stocks).validateSelectedStock(4L,3L,null,null,100L,101L,102L,BigDecimal.ONE);
        verify(areas).validateWarehouseAreaExists(200L,201L,202L);
        assertEquals(200L, mapper.selectById(id).getWarehouseId());
    }
    @Test void physicalWarehouseStockCannotMasqueradeAsLineSideReturn() {
        source.setWarehouseId(200L);
        assertThrows(com.aicloud.framework.common.exception.ServiceException.class, () -> service.createReturnIssueDetail(request));
        verify(stocks, never()).validateSelectedStock(any(),any(),any(),any(),any(),any(),any(),any());
    }
}
