package com.aicloud.framework.xss.core.clean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.regex.Pattern;

/**
 * 基于 JSONP 实现 XSS 过滤字符串
 */
public class JsoupXssCleaner implements XssCleaner {

    private static final Pattern HTML = Pattern.compile("<\\s*[/!a-zA-Z]", Pattern.CASE_INSENSITIVE);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final Safelist safelist;

    /**
     * 用于在 src 属性使用相对路径时，强制转换为绝对路径。 为空时不处理，值应为绝对路径的前缀（包含协议部分）
     */
    private final String baseUri;

    /**
     * 无参构造，默认使用 {@link JsoupXssCleaner#buildSafelist} 方法构建一个安全列表
     */
    public JsoupXssCleaner() {
        this.safelist = buildSafelist();
        this.baseUri = "https://aicloud.invalid";
    }

    /**
     * 构建一个 Xss 清理的 Safelist 规则。
     * 基于 Safelist#relaxed() 的基础上:
     * 1. 支持 class；移除 style，避免通过 CSS 注入脚本。
     * 2. a 标签额外支持了 target 属性
     * 3. 图片仅使用安全的网络协议或相对路径。
     *
     * @return Safelist
     */
    private Safelist buildSafelist() {
        // 使用 jsoup 提供的默认的
        Safelist relaxedSafelist = Safelist.relaxed();
        relaxedSafelist.addAttributes(":all", "class");
        // 保留 a 标签的 target 属性
        relaxedSafelist.addAttributes("a", "target");
        relaxedSafelist.addEnforcedAttribute("a", "rel", "noopener noreferrer");
        relaxedSafelist.preserveRelativeLinks(true);

        return relaxedSafelist;
    }

    @Override
    public String clean(String html) {
        if (html == null) return null;
        String trimmed = html.trim();
        // IM and form content can be a JSON string. Clean text values, not the JSON envelope.
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                JsonNode node = JSON.readTree(html);
                if (cleanJson(node, 0)) return JSON.writeValueAsString(node);
                return html;
            } catch (java.io.IOException invalidJson) { /* The consuming business parser validates malformed JSON. */ }
        }
        if (!HTML.matcher(html).find()) return html; // Do not HTML-encode URLs, formulas or ordinary text.
        return cleanHtml(html);
    }

    private String cleanHtml(String html) {
        return Jsoup.clean(html, baseUri, safelist, new Document.OutputSettings().prettyPrint(false));
    }

    private boolean cleanJson(JsonNode node, int depth) {
        if (depth > 64) throw new IllegalArgumentException("JSON content is too deeply nested");
        boolean changed = false;
        if (node instanceof ObjectNode object) {
            var fields = object.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                if (field.getValue().isTextual() && HTML.matcher(field.getValue().textValue()).find()) {
                    String clean = cleanHtml(field.getValue().textValue());
                    changed |= !clean.equals(field.getValue().textValue());
                    object.put(field.getKey(), clean);
                } else changed |= cleanJson(field.getValue(), depth + 1);
            }
        } else if (node instanceof ArrayNode array) {
            for (int i = 0; i < array.size(); i++) {
                JsonNode value = array.get(i);
                if (value.isTextual() && HTML.matcher(value.textValue()).find()) {
                    String clean = cleanHtml(value.textValue());
                    changed |= !clean.equals(value.textValue());
                    array.set(i, JSON.getNodeFactory().textNode(clean));
                } else changed |= cleanJson(value, depth + 1);
            }
        }
        return changed;
    }

}

