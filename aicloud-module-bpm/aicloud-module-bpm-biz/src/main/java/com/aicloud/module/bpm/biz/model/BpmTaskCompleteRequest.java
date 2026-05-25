package com.aicloud.module.bpm.biz.model; import jakarta.validation.constraints.NotNull; /**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class BpmTaskCompleteRequest { @NotNull private Long taskId; public Long getTaskId(){return taskId;} public void setTaskId(Long v){taskId=v;} }