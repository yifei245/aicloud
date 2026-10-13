package com.aicloud.module.crm.service.receivable;

import com.aicloud.framework.test.core.ut.BaseMockitoUnitTest;
import com.aicloud.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanSaveReqVO;
import com.aicloud.module.crm.dal.dataobject.contract.CrmContractDO;
import com.aicloud.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import com.aicloud.module.crm.dal.mysql.receivable.CrmReceivablePlanMapper;
import com.aicloud.module.crm.service.contract.CrmContractService;
import com.aicloud.module.crm.service.permission.CrmPermissionService;
import com.aicloud.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Objects;

import static com.aicloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.aicloud.framework.test.core.util.AssertUtils.assertServiceException;
import static com.aicloud.module.crm.enums.ErrorCodeConstants.CONTRACT_NOT_EXISTS;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class CrmReceivablePlanServiceImplTest extends BaseMockitoUnitTest {
    @InjectMocks
    private CrmReceivablePlanServiceImpl service;
    @Mock
    private CrmReceivablePlanMapper receivablePlanMapper;
    @Mock
    private CrmContractService contractService;
    @Mock
    private CrmPermissionService permissionService;
    @Mock
    private AdminUserApi adminUserApi;

    @Test
    void missingContractReturnsBusinessErrorBeforeInsert() {
        when(contractService.validateContract(9L)).thenThrow(exception(CONTRACT_NOT_EXISTS));
        assertServiceException(() -> service.createReceivablePlan(
                new CrmReceivablePlanSaveReqVO().setContractId(9L)), CONTRACT_NOT_EXISTS);
        verifyNoInteractions(receivablePlanMapper, permissionService);
    }

    @Test
    void validContractDeterminesCustomer() {
        when(contractService.validateContract(9L)).thenReturn(new CrmContractDO().setId(9L).setCustomerId(20L));
        service.createReceivablePlan(new CrmReceivablePlanSaveReqVO().setContractId(9L).setCustomerId(999L));
        verify(receivablePlanMapper).insert(argThat((CrmReceivablePlanDO row) ->
                Objects.equals(row.getCustomerId(), 20L) && Objects.equals(row.getPeriod(), 1)));
    }
}
