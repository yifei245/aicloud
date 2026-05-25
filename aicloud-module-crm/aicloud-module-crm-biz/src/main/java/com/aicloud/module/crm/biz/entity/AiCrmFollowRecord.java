package com.aicloud.module.crm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType; import com.baomidou.mybatisplus.annotation.TableId; import com.baomidou.mybatisplus.annotation.TableName;
@TableName("ai_crm_follow_record") /**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class AiCrmFollowRecord { @TableId(type = IdType.AUTO) private Long id; private Long tenantId; private Long customerId; private String followType; private String content; private java.time.LocalDateTime nextFollowTime; private String createBy; private java.time.LocalDateTime createTime; public Long getId(){return id;} public void setId(Long v){id=v;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public Long getCustomerId(){return customerId;} public void setCustomerId(Long v){customerId=v;} public String getFollowType(){return followType;} public void setFollowType(String v){followType=v;} public String getContent(){return content;} public void setContent(String v){content=v;} public java.time.LocalDateTime getNextFollowTime(){return nextFollowTime;} public void setNextFollowTime(java.time.LocalDateTime v){nextFollowTime=v;} public String getCreateBy(){return createBy;} public void setCreateBy(String v){createBy=v;} public java.time.LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(java.time.LocalDateTime v){createTime=v;} }
