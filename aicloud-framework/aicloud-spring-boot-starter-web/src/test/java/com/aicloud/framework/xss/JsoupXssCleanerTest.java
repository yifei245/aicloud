package com.aicloud.framework.xss;

import com.aicloud.framework.xss.core.clean.JsoupXssCleaner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsoupXssCleanerTest {
    private final JsoupXssCleaner cleaner = new JsoupXssCleaner();

    @Test void removesExecutableMarkupAndUnsafeCssButKeepsRichText() {
        String result = cleaner.clean("<p class='note'>hello <strong>world</strong><script>alert(1)</script>"
                + "<img src='/file.png' onerror='alert(2)' style='background:url(javascript:alert(3))'>"
                + "<a href='jav&#97;script:alert(4)' target='_blank'>link</a><iframe src='https://bad.example'></iframe></p>");
        assertTrue(result.contains("<strong>world</strong>"));
        assertTrue(result.contains("src=\"/file.png\""));
        for (String value : new String[]{"script", "onerror", "style=", "iframe"}) assertFalse(result.contains(value), result);
        assertTrue(result.contains("noopener noreferrer"));
    }

    @Test void ordinaryTextAndSignedUrlsAreUnchanged() {
        String url = "http://localhost/file?expires=123&signature=abcdef";
        assertEquals(url, cleaner.clean(url));
        assertEquals("价格 1 < 2 & 数量 3", cleaner.clean("价格 1 < 2 & 数量 3"));
    }

    @Test void embeddedJsonStaysValidAndUnicodeEscapedScriptIsRemoved() throws Exception {
        String raw = "{\"text\":\"\\u003cscript\\u003ealert(1)\\u003c/script\\u003e<p>safe</p>\",\"url\":\"http://localhost?a=1&b=2\"}";
        var json = new ObjectMapper().readTree(cleaner.clean(raw));
        assertEquals("<p>safe</p>", json.get("text").textValue());
        assertEquals("http://localhost?a=1&b=2", json.get("url").textValue());
    }
}
