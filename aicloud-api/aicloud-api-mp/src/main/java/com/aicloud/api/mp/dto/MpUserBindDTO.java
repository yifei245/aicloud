package com.aicloud.api.mp.dto;

/**
 * Mini program user bind DTO.
 *
 * @author yifei
 */
public class MpUserBindDTO {

    private Long tenantId;
    private Long userId;
    private String openId;
    private String unionId;
    private String nickname;
    private String avatarUrl;
    private String phone;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }
    public String getUnionId() { return unionId; }
    public void setUnionId(String unionId) { this.unionId = unionId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    
    public String toString() {
        return "MpUserBindDTO{}";
    }
}
