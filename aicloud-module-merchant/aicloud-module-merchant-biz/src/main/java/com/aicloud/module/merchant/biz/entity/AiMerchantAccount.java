package com.aicloud.module.merchant.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType; import com.baomidou.mybatisplus.annotation.TableId; import com.baomidou.mybatisplus.annotation.TableName; import java.time.LocalDateTime;
@TableName("ai_merchant_account") /**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class AiMerchantAccount { @TableId(type = IdType.AUTO) private Long id; private Long tenantId; private Long merchantId; private String username; private String nickname; private String mobile; private String roleCode; private String status; private LocalDateTime createTime; private LocalDateTime updateTime; public Long getId(){return id;} public void setId(Long v){id=v;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public Long getMerchantId(){return merchantId;} public void setMerchantId(Long v){merchantId=v;} public String getUsername(){return username;} public void setUsername(String v){username=v;} public String getNickname(){return nickname;} public void setNickname(String v){nickname=v;} public String getMobile(){return mobile;} public void setMobile(String v){mobile=v;} public String getRoleCode(){return roleCode;} public void setRoleCode(String v){roleCode=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime v){createTime=v;} public LocalDateTime getUpdateTime(){return updateTime;} public void setUpdateTime(LocalDateTime v){updateTime=v;} }
