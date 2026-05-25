package com.aicloud.module.system.biz.model.user;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class UserResponse {
    private Long id;
    private Long tenantId;
    private String username;
    private String nickname;
    private String mobile;
    private String email;
    private String userType;
    private Integer status;
    private Long deptId;
    private String deptName;
    private List<Long> postIds;
    private List<String> postNames;
    private List<Long> roleIds;
    private List<String> roleCodes;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public List<Long> getPostIds() { return postIds; }
    public void setPostIds(List<Long> postIds) { this.postIds = postIds; }
    public List<String> getPostNames() { return postNames; }
    public void setPostNames(List<String> postNames) { this.postNames = postNames; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    public List<String> getRoleCodes() { return roleCodes; }
    public void setRoleCodes(List<String> roleCodes) { this.roleCodes = roleCodes; }
    public LocalDateTime getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(LocalDateTime lastLoginTime) { this.lastLoginTime = lastLoginTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
