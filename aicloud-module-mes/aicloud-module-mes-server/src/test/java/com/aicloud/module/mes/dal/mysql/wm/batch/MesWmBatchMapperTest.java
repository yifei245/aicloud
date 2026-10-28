package com.aicloud.module.mes.dal.mysql.wm.batch;

import com.aicloud.framework.test.core.ut.BaseDbUnitTest;
import com.aicloud.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MesWmBatchMapperTest extends BaseDbUnitTest {
    @Resource MesWmBatchMapper mapper;
    @Test void omittedDimensionsStillFindBatchAfterMysqlDefaultZeroRoundTrip() {
        var batch = new MesWmBatchDO().setItemId(40001L).setCode("B-40001"); mapper.insert(batch);
        assertEquals(batch.getId(), mapper.selectFirst(new MesWmBatchDO().setItemId(40001L)).getId());
    }
    @Test void explicitDimensionDoesNotMatchAnUnspecifiedBatch() {
        var batch = new MesWmBatchDO().setItemId(40002L).setCode("B-40002").setVendorId(10L); mapper.insert(batch);
        assertNull(mapper.selectFirst(new MesWmBatchDO().setItemId(40002L)));
        assertEquals(batch.getId(), mapper.selectFirst(new MesWmBatchDO().setItemId(40002L).setVendorId(10L)).getId());
    }
}
