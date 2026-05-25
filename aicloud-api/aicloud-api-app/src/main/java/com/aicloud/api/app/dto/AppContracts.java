package com.aicloud.api.app.dto;

/**
 * App API path contracts.
 *
 * @author yifei
 */
public final class AppContracts {

    public static final String AUTH_LOGIN = "/app/auth/login";
    public static final String MEMBER_PROFILE = "/app/member/profile";
    public static final String PRODUCT_LIST = "/app/product/list";
    public static final String CART = "/app/trade/cart";
    public static final String ORDER = "/app/trade/order";
    public static final String PAY = "/app/pay/order";

    private AppContracts() {
    }
}
