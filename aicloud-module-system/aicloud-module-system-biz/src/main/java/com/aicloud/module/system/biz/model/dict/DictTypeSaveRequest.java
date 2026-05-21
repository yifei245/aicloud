package com.aicloud.module.system.biz.model.dict;

public class DictTypeSaveRequest {
    private Long id;
    private String dictType;
    private String dictName;
    private Integer status;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }
    public String getDictName() { return dictName; }
    public void setDictName(String dictName) { this.dictName = dictName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
