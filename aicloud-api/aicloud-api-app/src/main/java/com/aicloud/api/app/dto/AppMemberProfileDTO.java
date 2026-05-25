package com.aicloud.api.app.dto;

import java.math.BigDecimal;

/**
 * App member profile DTO.
 *
 * @author yifei
 */
public class AppMemberProfileDTO {

    private Long userId;
    private String nickname;
    private String mobile;
    private String avatar;
    private String level;
    private Integer points;
    private BigDecimal balance;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    
    public String toString() {
        return "AppMemberProfileDTO{}";
    }
}
