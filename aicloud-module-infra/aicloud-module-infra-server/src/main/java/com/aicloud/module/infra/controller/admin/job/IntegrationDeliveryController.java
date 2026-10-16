package com.aicloud.module.infra.controller.admin.job;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/infra/integration")
@Tag(name="管理后台 - 跨模块业务投递")
public class IntegrationDeliveryController {
    @Resource private JdbcTemplate jdbc;
    @GetMapping("/pending") @Operation(summary="查询本租户尚未完成的业务联动")
    @PreAuthorize("@ss.hasPermission('infra:job:query')")
    public CommonResult<List<Map<String,Object>>> pending() {
        return CommonResult.success(jdbc.queryForList("SELECT id,producer,event_type,business_key,status,attempts,last_error,next_attempt_time,create_time FROM integration_outbox WHERE tenant_id=? AND status<>'DONE' ORDER BY id LIMIT 200",TenantContextHolder.getRequiredTenantId()));
    }
    @PostMapping("/retry") @Operation(summary="修正映射或配置后，立即重试失败的业务联动")
    @PreAuthorize("@ss.hasPermission('infra:job:update')")
    public CommonResult<Boolean> retry(@RequestParam Long id) {
        int changed=jdbc.update("UPDATE integration_outbox SET next_attempt_time=CURRENT_TIMESTAMP WHERE tenant_id=? AND id=? AND status='RETRY'",TenantContextHolder.getRequiredTenantId(),id);
        if(changed==0) throw new IllegalArgumentException("未找到可重试的业务投递记录");
        return CommonResult.success(true);
    }
}
