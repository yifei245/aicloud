package com.aicloud.api.admin.dto;

import java.util.List;

/**
 * Admin role DTO.
 *
 * @author yifei
 */
public class AdminRoleDTO {

    private Long id;
    private Long tenantId;
    private String roleCode;
    private String roleName;
    private String dataScope;
    private List<Long> menuIds;
    private List<Long> deptIds;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDataScope() { return dataScope; }
    public void setDataScope(String dataScope) { this.dataScope = dataScope; }
    public List<Long> getMenuIds() { return menuIds; }
    public void setMenuIds(List<Long> menuIds) { this.menuIds = menuIds; }
    public List<Long> getDeptIds() { return deptIds; }
    public void setDeptIds(List<Long> deptIds) { this.deptIds = deptIds; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    
    public String toString() {
        return "AdminRoleDTO{}";
    }
}
