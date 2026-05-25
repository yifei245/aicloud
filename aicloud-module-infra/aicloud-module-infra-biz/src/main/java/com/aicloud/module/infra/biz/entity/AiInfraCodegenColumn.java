package com.aicloud.module.infra.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("ai_infra_codegen_column")
/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class AiInfraCodegenColumn {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long tableId;
    private String columnName;
    private String javaField;
    private String javaType;
    private String queryType;
    private Integer formShow;
    private Integer listShow;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }
    public String getJavaField() { return javaField; }
    public void setJavaField(String javaField) { this.javaField = javaField; }
    public String getJavaType() { return javaType; }
    public void setJavaType(String javaType) { this.javaType = javaType; }
    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }
    public Integer getFormShow() { return formShow; }
    public void setFormShow(Integer formShow) { this.formShow = formShow; }
    public Integer getListShow() { return listShow; }
    public void setListShow(Integer listShow) { this.listShow = listShow; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
