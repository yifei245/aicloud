package com.aicloud.framework.xss;

import com.aicloud.framework.xss.config.XssProperties;
import com.aicloud.framework.xss.core.clean.JsoupXssCleaner;
import com.aicloud.framework.xss.core.json.XssStringJsonDeserializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class XssStringJsonDeserializerTest {
    private ObjectMapper mapper() {
        var module = new SimpleModule().addDeserializer(String.class,
                new XssStringJsonDeserializer(new XssProperties(), new AntPathMatcher(), new JsoupXssCleaner()));
        return new ObjectMapper().registerModule(module);
    }
    @AfterEach void cleanup() { RequestContextHolder.resetRequestAttributes(); }
    @Test void databaseStringArraysAndStoredMarkupRemainReadableOutsideRequests() throws Exception {
        assertEquals(List.of("password", "<b>stored</b>"), mapper().readValue("[\"password\",\"<b>stored</b>\"]", new TypeReference<List<String>>() {}));
    }
    @Test void rootArraysInsideRequestsHaveNoFieldNameAndAreSanitized() throws Exception {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest("POST", "/admin-api/im/message/send")));
        assertEquals(List.of("<p>safe</p>"), mapper().readValue("[\"<script>bad()</script><p>safe</p>\"]", new TypeReference<List<String>>() {}));
    }
    @Test void credentialsArePreservedWhileRichTextIsSanitized() throws Exception {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest("POST", "/admin-api/test")));
        Map<String,String> values = mapper().readValue("{\"password\":\"<secret>\",\"content\":\"<script>bad()</script><p>safe</p>\"}", new TypeReference<>() {});
        assertEquals("<secret>", values.get("password"));
        assertEquals("<p>safe</p>", values.get("content"));
    }
}
