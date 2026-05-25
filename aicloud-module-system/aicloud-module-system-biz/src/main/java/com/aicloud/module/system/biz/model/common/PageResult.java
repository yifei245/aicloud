package com.aicloud.module.system.biz.model.common;

import com.aicloud.common.pojo.PageResponse;
import java.util.List;

/**
 * Compatibility wrapper for old system page result.
 *
 * @author yifei
 */
@Deprecated
public class PageResult<T> extends PageResponse<T> {

    public PageResult() {
    }

    public PageResult(long total, List<T> list) {
        super(total, 1, total, list);
    }

    public PageResult(long total, long pageNo, long pageSize, List<T> list) {
        super(total, pageNo, pageSize, list);
    }
}
