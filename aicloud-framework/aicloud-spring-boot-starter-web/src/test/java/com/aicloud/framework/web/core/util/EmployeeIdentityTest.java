package com.aicloud.framework.web.core.util;
import com.aicloud.framework.common.enums.UserTypeEnum;
import com.aicloud.framework.web.config.WebProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
class EmployeeIdentityTest {
    @Test void employeeMemberAndApplicationRoutesRequireDistinctIdentityTypes() {
        new WebFrameworkUtils(new WebProperties());
        assertEquals(UserTypeEnum.ADMIN.getValue(),type("/employee-api/hrm/profile/get"));
        assertEquals(UserTypeEnum.ADMIN.getValue(),type("/admin-api/hrm/employee/get"));
        assertEquals(UserTypeEnum.MEMBER.getValue(),type("/app-api/member/user/get"));
        assertEquals(UserTypeEnum.APPLICATION.getValue(),type("/open-api/v1/system/dictionaries/type"));
        assertNull(type("/employee-api-forged/hrm/profile/get"));
    }
    private Integer type(String path) {
        var request=new MockHttpServletRequest();request.setServletPath(path);return WebFrameworkUtils.getLoginUserType(request);
    }
}
