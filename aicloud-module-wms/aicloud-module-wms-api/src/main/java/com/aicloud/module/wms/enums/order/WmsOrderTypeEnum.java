package com.aicloud.module.wms.enums.order;

import com.aicloud.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * WMS 单据类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum WmsOrderTypeEnum implements ArrayValuable<Integer> {

    RECEIPT(1, "入库单"),
    SHIPMENT(2, "出库单"),
    MOVEMENT(3, "移库单"),
    CHECK(4, "盘库单"),
    ERP(90, "ERP 库存单据"),
    MES(91, "MES 生产事务"),
    MALL(92, "商城发货/退货");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(WmsOrderTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
