package com.aicloud.module.crm.service.permission;

import com.aicloud.framework.test.core.ut.BaseMockitoUnitTest;
import com.aicloud.module.crm.dal.dataobject.permission.CrmPermissionDO;
import com.aicloud.module.crm.dal.mysql.permission.CrmPermissionMapper;
import com.aicloud.module.crm.enums.permission.CrmPermissionLevelEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CrmPermissionServiceImplTest extends BaseMockitoUnitTest {
    @InjectMocks private CrmPermissionServiceImpl service;
    @Mock private CrmPermissionMapper mapper;

    private void grant(CrmPermissionLevelEnum level) {
        CrmPermissionDO permission = new CrmPermissionDO();
        permission.setUserId(1L);
        permission.setLevel(level.getLevel());
        when(mapper.selectByBizTypeAndBizId(2, 10L)).thenReturn(List.of(permission));
    }

    @Test void ownerCanReadAndWrite() {
        grant(CrmPermissionLevelEnum.OWNER);
        for (CrmPermissionLevelEnum level : CrmPermissionLevelEnum.values())
            assertTrue(service.hasPermission(2, 10L, 1L, level));
    }

    @Test void writerCanReadButCannotOwn() {
        grant(CrmPermissionLevelEnum.WRITE);
        assertTrue(service.hasPermission(2, 10L, 1L, CrmPermissionLevelEnum.READ));
        assertTrue(service.hasPermission(2, 10L, 1L, CrmPermissionLevelEnum.WRITE));
        assertFalse(service.hasPermission(2, 10L, 1L, CrmPermissionLevelEnum.OWNER));
    }

    @Test void readerCannotWriteOrOwn() {
        grant(CrmPermissionLevelEnum.READ);
        assertTrue(service.hasPermission(2, 10L, 1L, CrmPermissionLevelEnum.READ));
        assertFalse(service.hasPermission(2, 10L, 1L, CrmPermissionLevelEnum.WRITE));
        assertFalse(service.hasPermission(2, 10L, 1L, CrmPermissionLevelEnum.OWNER));
    }

    @Test void anotherUserCannotUseOwnersPermission() {
        grant(CrmPermissionLevelEnum.OWNER);
        assertFalse(service.hasPermission(2, 10L, 2L, CrmPermissionLevelEnum.READ));
    }
}
