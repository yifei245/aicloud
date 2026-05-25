package com.aicloud.api.open.dto;

/**
 * Third-party OpenAPI contracts.
 *
 * @author yifei
 */
public final class OpenContracts {

    public static final String PING = "/openapi/v1/ping";
    public static final String PRODUCT_SYNC = "/openapi/v1/product/sync";
    public static final String ORDER_STATUS = "/openapi/v1/order/status";
    public static final String WEBHOOK_EVENT = "/openapi/v1/webhook/event";

    private OpenContracts() {
    }
}
