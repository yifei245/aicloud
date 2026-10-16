package com.aicloud.framework.datapermission.core.rpc;

import com.aicloud.framework.datapermission.core.aop.DataPermissionContextHolder;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import static org.junit.jupiter.api.Assertions.*;

class DataPermissionRpcWebFilterTest {
    @AfterEach void clear() { DataPermissionContextHolder.clear(); }
    @Test void publicRequestsCannotDisablePermissionsEvenWithAServiceCredential() throws Exception {
        verify("/admin-api/hrm/employee/page", "correct-service-secret", false);
        verify("/app-api/member/user/get", null, false);
        verify("/employee-api/hrm/profile", null, false);
    }
    @Test void unauthenticatedRpcCannotDisablePermissions() throws Exception {
        verify("/rpc-api/hrm/employee/get", null, false);
        verify("/rpc-api/hrm/employee/get", "wrong-secret", false);
    }
    @Test void authenticatedRpcCanPropagateTrustedPermissionContextWithoutLeakingIt() throws Exception {
        verify("/rpc-api/hrm/employee/get", "correct-service-secret", true);
        assertNull(DataPermissionContextHolder.get());
    }
    private void verify(String path,String secret,boolean ignored) throws Exception {
        var request=new MockHttpServletRequest("GET",path);
        request.addHeader("data-permission-enable","false");
        if(secret!=null) request.addHeader("X-Aicloud-Internal-Token",secret);
        new DataPermissionRpcWebFilter("correct-service-secret").doFilter(request,new MockHttpServletResponse(),(req,res)-> {
            var permission=DataPermissionContextHolder.get();
            assertEquals(ignored,permission!=null && !permission.enable());
        });
        assertNull(DataPermissionContextHolder.get());
    }
}
