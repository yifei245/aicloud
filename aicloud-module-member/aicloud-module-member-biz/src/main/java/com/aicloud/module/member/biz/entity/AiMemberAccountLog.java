package com.aicloud.module.member.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("ai_member_account_log")
public class AiMemberAccountLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long userId;
    private String bizType;
    private String changeType;
    private Long pointsDelta;
    private BigDecimal balanceDelta;
    private Long pointsAfter;
    private BigDecimal balanceAfter;
    private String remark;
    private LocalDateTime createTime;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public Long getPointsDelta() { return pointsDelta; }
    public void setPointsDelta(Long pointsDelta) { this.pointsDelta = pointsDelta; }
    public BigDecimal getBalanceDelta() { return balanceDelta; }
    public void setBalanceDelta(BigDecimal balanceDelta) { this.balanceDelta = balanceDelta; }
    public Long getPointsAfter() { return pointsAfter; }
    public void setPointsAfter(Long pointsAfter) { this.pointsAfter = pointsAfter; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
