USE aicloud;
INSERT INTO system_notify_template(name,code,nickname,content,type,params,status,remark)
SELECT '审批待办','bpm_task_assigned','aicloud 审批','{startUserNickname} 发起的「{processInstanceName}」有待办任务「{taskName}」，查看：{detailUrl}',2,'["startUserNickname","processInstanceName","taskName","detailUrl"]',0,'审批主通知通道，不依赖短信'
WHERE NOT EXISTS(SELECT 1 FROM system_notify_template WHERE code='bpm_task_assigned' AND deleted=0);
INSERT INTO system_notify_template(name,code,nickname,content,type,params,status,remark)
SELECT '审批通过','bpm_process_instance_approve','aicloud 审批','你的申请「{processInstanceName}」已通过，查看：{detailUrl}',2,'["processInstanceName","detailUrl"]',0,'审批主通知通道，不依赖短信'
WHERE NOT EXISTS(SELECT 1 FROM system_notify_template WHERE code='bpm_process_instance_approve' AND deleted=0);
INSERT INTO system_notify_template(name,code,nickname,content,type,params,status,remark)
SELECT '审批驳回','bpm_process_instance_reject','aicloud 审批','你的申请「{processInstanceName}」已驳回，原因：{reason}，查看：{detailUrl}',2,'["processInstanceName","reason","detailUrl"]',0,'审批主通知通道，不依赖短信'
WHERE NOT EXISTS(SELECT 1 FROM system_notify_template WHERE code='bpm_process_instance_reject' AND deleted=0);
INSERT INTO system_notify_template(name,code,nickname,content,type,params,status,remark)
SELECT '审批超时','bpm_task_timeout','aicloud 审批','流程「{processInstanceName}」的任务「{taskName}」已超时，查看：{detailUrl}',2,'["processInstanceName","taskName","detailUrl"]',0,'审批主通知通道，不依赖短信'
WHERE NOT EXISTS(SELECT 1 FROM system_notify_template WHERE code='bpm_task_timeout' AND deleted=0);
-- Only complete the aicloud demonstration department; preserve configured leaders.
UPDATE system_dept SET leader_user_id=1 WHERE id=1 AND tenant_id=1 AND name='aicloud' AND parent_id=0 AND leader_user_id IS NULL AND deleted=0;

-- Local business notifications. Existing templates with the same code are preserved.
INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '会议提醒','oa_meeting_room_booking_reminder','aicloud 办公','会议「{title}」将于 {startTime} 在 {roomName}（{location}）开始。',2,'["title","startTime","roomName","location"]',0,'OA 本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='oa_meeting_room_booking_reminder' AND deleted=0);
INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '日程提醒','oa_schedule_reminder','aicloud 办公','日程「{scheduleTitle}」开始时间：{startTime}，查看：{route}',2,'["scheduleTitle","startTime","route"]',0,'OA 本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='oa_schedule_reminder' AND deleted=0);

INSERT INTO system_menu (name,permission,type,parent_id,sort)
SELECT '登记用印','oa:seal-apply:use',3,m.parent_id,90 FROM system_menu m
WHERE m.permission='oa:seal-apply:create' AND m.deleted=0
AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission='oa:seal-apply:use' AND deleted=0) LIMIT 1;
INSERT INTO system_menu (name,permission,type,parent_id,sort)
SELECT '确认归还印章','oa:seal-apply:return',3,m.parent_id,91 FROM system_menu m
WHERE m.permission='oa:seal-apply:create' AND m.deleted=0
AND NOT EXISTS (SELECT 1 FROM system_menu WHERE permission='oa:seal-apply:return' AND deleted=0) LIMIT 1;

INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '招聘面试安排','hrm_recruit_interview_arranged','aicloud 人事','候选人 {candidateName} 的第 {stageNumber} 轮面试时间为 {interviewTime}。',2,'["candidateName","stageNumber","interviewTime"]',0,'本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='hrm_recruit_interview_arranged' AND deleted=0);
INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '员工端开通','hrm_employee_opened','aicloud 人事','{employeeName}，员工端已开通，可以使用本人账号登录。',2,'["employeeName"]',0,'本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='hrm_employee_opened' AND deleted=0);
INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '填写员工档案','hrm_employee_archive_fill','aicloud 人事','{employeeName}，请登录员工端完善本人档案。',2,'["employeeName"]',0,'本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='hrm_employee_archive_fill' AND deleted=0);
INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '社保月表通知','hrm_insurance_month_record_created','aicloud 人事','{employeeName}，{year} 年 {month} 月社保记录已生成，查看：{route}',2,'["employeeName","year","month","route"]',0,'本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='hrm_insurance_month_record_created' AND deleted=0);
INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '绩效待办','hrm_performance_task_pending','aicloud 人事','绩效计划「{planName}」有待办阶段「{stageName}」，查看：{route}',2,'["planName","stageName","route"]',0,'本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='hrm_performance_task_pending' AND deleted=0);
INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '绩效处理结果','hrm_performance_process_result','aicloud 人事','绩效计划「{planName}」已执行 {actionName}，结果：{result}，查看：{route}',2,'["planName","actionName","result","route"]',0,'本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='hrm_performance_process_result' AND deleted=0);
INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '工资条发放','hrm_salary_slip_sent','aicloud 人事','{employeeName}，{year} 年 {month} 月工资条已发放，实发金额 {realSalary} 元。',2,'["employeeName","year","month","realSalary"]',0,'本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='hrm_salary_slip_sent' AND deleted=0);
INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '项目工作项指派','pms_work_item_assigned','aicloud 项目','项目「{projectName}」工作项 {serialNumber}「{workItemName}」已指派给你，查看：{route}',2,'["projectName","serialNumber","workItemName","route"]',0,'本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='pms_work_item_assigned' AND deleted=0);
INSERT INTO system_notify_template (name,code,nickname,content,type,params,status,remark)
SELECT '知识文档共享','pms_knowledge_document_shared','aicloud 项目','知识文档「{documentTitle}」已共享给你，查看：{route}',2,'["documentTitle","route"]',0,'本地业务通知'
WHERE NOT EXISTS (SELECT 1 FROM system_notify_template WHERE code='pms_knowledge_document_shared' AND deleted=0);
