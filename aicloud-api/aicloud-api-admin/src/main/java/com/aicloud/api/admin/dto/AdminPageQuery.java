package com.aicloud.api.admin.dto;

/**
 * Admin page query.
 *
 * @author yifei
 */
public class AdminPageQuery {

    private Long tenantId;
    private String keyword;
    private Long pageNo = 1L;
    private Long pageSize = 10L;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getPageNo() { return pageNo; }
    public void setPageNo(Long pageNo) { this.pageNo = pageNo; }
    public Long getPageSize() { return pageSize; }
    public void setPageSize(Long pageSize) { this.pageSize = pageSize; }

    
    public String toString() {
        return "AdminPageQuery{}";
    }
}
