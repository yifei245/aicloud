package com.aicloud.module.system.biz.model.user;

import java.util.List;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class UserSaveRequest {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String mobile;
    private String email;
    private String userType;
    private Integer status;
    private Long deptId;
    private List<Long> postIds;
    private List<Long> roleIds;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
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
    public List<Long> getPostIds() { return postIds; }
    public void setPostIds(List<Long> postIds) { this.postIds = postIds; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
}
