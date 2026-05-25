package com.aicloud.module.system.biz.model.dept;

import java.util.ArrayList;
import java.util.List;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class DeptResponse {
    private Long id;
    private Long parentId;
    private String name;
    private Long leaderUserId;
    private String leaderNickname;
    private Integer sort;
    private Integer status;
    private List<DeptResponse> children = new ArrayList<>();
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getLeaderUserId() { return leaderUserId; }
    public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }
    public String getLeaderNickname() { return leaderNickname; }
    public void setLeaderNickname(String leaderNickname) { this.leaderNickname = leaderNickname; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<DeptResponse> getChildren() { return children; }
    public void setChildren(List<DeptResponse> children) { this.children = children; }
}
