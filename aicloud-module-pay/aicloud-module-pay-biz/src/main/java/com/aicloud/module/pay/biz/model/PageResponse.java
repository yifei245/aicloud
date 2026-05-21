package com.aicloud.module.pay.biz.model;

import java.util.List;

public class PageResponse<T> {
    private long total;
    private long pageNo;
    private long pageSize;
    private List<T> list;
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getPageNo() { return pageNo; }
    public void setPageNo(long pageNo) { this.pageNo = pageNo; }
    public long getPageSize() { return pageSize; }
    public void setPageSize(long pageSize) { this.pageSize = pageSize; }
    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
}
