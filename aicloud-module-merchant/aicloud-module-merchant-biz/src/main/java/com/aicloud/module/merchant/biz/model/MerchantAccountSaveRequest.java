package com.aicloud.module.merchant.biz.model;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull; /**
 * AICloud generated source.
 *
 * @author yifei
 */
public class MerchantAccountSaveRequest { private Long id; @NotNull private Long tenantId; @NotNull private Long merchantId; @NotBlank private String username; private String nickname; private String mobile; @NotBlank private String roleCode; @NotBlank private String status; public Long getId(){return id;} public void setId(Long v){id=v;} public Long getTenantId(){return tenantId;} public void setTenantId(Long v){tenantId=v;} public Long getMerchantId(){return merchantId;} public void setMerchantId(Long v){merchantId=v;} public String getUsername(){return username;} public void setUsername(String v){username=v;} public String getNickname(){return nickname;} public void setNickname(String v){nickname=v;} public String getMobile(){return mobile;} public void setMobile(String v){mobile=v;} public String getRoleCode(){return roleCode;} public void setRoleCode(String v){roleCode=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} }
