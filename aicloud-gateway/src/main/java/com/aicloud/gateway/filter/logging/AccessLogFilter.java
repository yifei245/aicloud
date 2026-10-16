package com.aicloud.gateway.filter.logging;

import com.aicloud.gateway.util.SecurityFrameworkUtils;
import com.aicloud.gateway.util.SkyWalkingTraceUtils;
import com.aicloud.gateway.util.WebFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Request metadata only: credentials and business payloads never enter access logs. */
@Slf4j
@Component
public class AccessLogFilter implements GlobalFilter, Ordered {
    @Override
    public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.nanoTime();
        return chain.filter(exchange).doFinally(signal -> SkyWalkingTraceUtils.callWithTraceIdInMdc(exchange, () -> {
            var route = WebFrameworkUtils.getGatewayRoute(exchange);
            var status = exchange.getResponse().getStatusCode();
            log.info("Gateway request: method={}, path={}, route={}, userId={}, userType={}, status={}, durationMs={}, outcome={}",
                    exchange.getRequest().getMethod(), exchange.getRequest().getURI().getRawPath(),
                    route == null ? null : route.getId(), SecurityFrameworkUtils.getLoginUserId(exchange),
                    SecurityFrameworkUtils.getLoginUserType(exchange), status == null ? null : status.value(),
                    (System.nanoTime() - start) / 1_000_000, signal);
            return null;
        }));
    }
}
