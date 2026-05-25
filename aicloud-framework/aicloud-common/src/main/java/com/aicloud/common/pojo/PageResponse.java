package com.aicloud.common.pojo;

import java.util.List;

/**
 * Common page response.
 *
 * @author yifei
 */
public class PageResponse<T> {

    private long total;
    private long pageNo;
    private long pageSize;
    private List<T> list;

    public PageResponse() {
    }

    public PageResponse(long total, long pageNo, long pageSize, List<T> list) {
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPageNo() {
        return pageNo;
    }

    public void setPageNo(long pageNo) {
        this.pageNo = pageNo;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
