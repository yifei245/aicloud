package com.aicloud.module.member.biz.model.address;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class MemberAddressSaveRequest {
    private Long id;
    private String receiverName;
    private String mobile;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer defaultStatus;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    public Integer getDefaultStatus() { return defaultStatus; }
    public void setDefaultStatus(Integer defaultStatus) { this.defaultStatus = defaultStatus; }
}
