package com.aicloud.module.system.biz.model.dept;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class DeptSaveRequest {
    private Long id;
    private Long parentId;
    private String name;
    private Long leaderUserId;
    private Integer sort;
    private Integer status;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getLeaderUserId() { return leaderUserId; }
    public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
