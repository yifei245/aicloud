package com.aicloud.framework.mybatis.config;

import com.aicloud.framework.tenant.config.TenantProperties;
import com.aicloud.framework.tenant.core.TenantContextHolder;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.util.StringUtils;

/**
 * MyBatis Plus tenant line handler.
 *
 * @author yifei
 */
public class AicloudTenantLineHandler implements TenantLineHandler {

    private static final String TENANT_ID_COLUMN = "tenant_id";

    private final TenantProperties tenantProperties;
    private final Set<String> ignoreTables;

    public AicloudTenantLineHandler(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
        this.ignoreTables = tenantProperties.getIgnoreTables().stream()
                .filter(StringUtils::hasText)
                .map(item -> item.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContextHolder.getTenantId());
    }

    @Override
    public String getTenantIdColumn() {
        return TENANT_ID_COLUMN;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (!tenantProperties.isEnabled() || TenantContextHolder.isTenantIgnored()) {
            return true;
        }
        return tableName != null && ignoreTables.contains(tableName.toLowerCase(Locale.ROOT));
    }
}
