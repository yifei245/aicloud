package com.aicloud.module.hrm.service.attendance.record;

import cn.hutool.crypto.digest.DigestUtil;
import com.aicloud.framework.common.util.json.JsonUtils;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.aicloud.module.hrm.api.business.HrmOaApi;
import com.aicloud.module.hrm.dal.dataobject.attendance.record.HrmAttendanceLeaveDO;
import com.aicloud.module.hrm.dal.mysql.attendance.record.HrmAttendanceLeaveMapper;
import com.aicloud.module.hrm.service.employee.info.HrmEmployeeService;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class HrmLeaveBalanceService {
    @Resource private JdbcTemplate jdbc;
    @Resource private HrmEmployeeService employees;
    @Resource private HrmAttendanceLeaveMapper leaves;
    @Resource private com.aicloud.framework.mybatis.core.integration.IntegrationInboxService inbox;

    @Transactional(rollbackFor=Exception.class)
    public Long reserveOa(HrmOaApi.Leave request) {
        return inbox.execute(tenant(),"hrm-oa-reserve",request.getKey(),request,Long.class,() -> reserveOaOnce(request));
    }
    private Long reserveOaOnce(HrmOaApi.Leave request) {
        var employee = employees.validateEmployeeBySelf(request.getUserId());
        initializePolicies();
        var policies = jdbc.queryForList("SELECT code FROM hrm_leave_policy WHERE tenant_id=? AND oa_type=?", tenant(), request.getOaType());
        if (policies.isEmpty()) throw new IllegalArgumentException("OA 假别尚未关联 HRM 假期规则");
        String code = (String) policies.getFirst().get("code");
        // Keep a single HRM attendance fact for OA approvals.
        var rows = jdbc.queryForList("SELECT leave_id FROM hrm_leave_reservation WHERE tenant_id=? AND source='OA' AND business_key=?", tenant(), request.getKey());
        Long leaveId = rows.isEmpty() || rows.getFirst().get("leave_id") == null ? null : ((Number) rows.getFirst().get("leave_id")).longValue();
        if (leaveId == null) {
            var record = new HrmAttendanceLeaveDO().setEmployeeId(employee.getId()).setType(code).setDay(request.getDays())
                    .setStartTime(request.getStartTime()).setEndTime(request.getEndTime()).setReason(request.getReason())
                    .setRemark("OA 来源 " + request.getSourceId()).setApprovalStatus(BpmProcessInstanceStatusEnum.RUNNING.getStatus());
            leaves.insert(record); leaveId = record.getId();
        }
        reserve("OA", request.getKey(), employee.getId(), code, request.getDays(), request.getStartTime(), request.getEndTime(), leaveId, request);
        return leaveId;
    }

    @Transactional(rollbackFor=Exception.class)
    public void reserveNative(HrmAttendanceLeaveDO record) {
        reserve("HRM", "leave:" + record.getId(), record.getEmployeeId(), record.getType(), record.getDay(),
                record.getStartTime(), record.getEndTime(), record.getId(), record);
    }
    private void reserve(String source, String key, Long employee, String code, BigDecimal days, LocalDateTime start, LocalDateTime end, Long leaveId, Object payload) {
        if (days == null || days.signum() <= 0 || !end.isAfter(start) || start.getYear() != end.getYear()) throw new IllegalArgumentException("请假时长无效；跨年度请拆分申请");
        employees.validateEmployeeExistsForUpdate(employee);
        initializePolicies();
        var policy = jdbc.queryForList("SELECT limited FROM hrm_leave_policy WHERE tenant_id=? AND code=?", tenant(), code);
        if (policy.isEmpty()) throw new IllegalArgumentException("请先配置 HRM 假别及额度规则");
        boolean limited = ((Number) policy.getFirst().get("limited")).intValue() == 1;
        String hash = DigestUtil.sha256Hex(JsonUtils.toJsonString(payload));
        jdbc.update("INSERT INTO hrm_leave_reservation(tenant_id,source,business_key,payload_hash,state) VALUES(?,?,?,?,'NEW') ON DUPLICATE KEY UPDATE id=id", tenant(), source, key, hash);
        var row = reservation(source,key);
        if ("CANCELED".equals(row.get("state"))) throw new IllegalArgumentException("请假预占已经撤销");
        if (!hash.equals(row.get("payload_hash"))) throw new IllegalArgumentException("请假来源对应不同内容");
        if (!"NEW".equals(row.get("state"))) return;
        long conflicts = leaves.selectCountByEmployeeIdAndTimeRangeAndApprovalStatusIn(employee, new LocalDateTime[]{start,end},
                List.of(BpmProcessInstanceStatusEnum.RUNNING.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus()));
        // This new record is already inserted; anything beyond it is an overlap from either OA or HRM.
        if (conflicts > 1) throw new IllegalArgumentException("与已有 OA/HRM 请假时间重叠");
        if (limited) {
            var balance = lockBalance(employee,code,start.getYear());
            BigDecimal available = decimal(balance,"entitlement").subtract(decimal(balance,"used")).subtract(decimal(balance,"reserved"));
            if (available.compareTo(days) < 0) throw new IllegalArgumentException("假期可用余额不足：" + available);
            jdbc.update("UPDATE hrm_leave_balance SET reserved=reserved+? WHERE id=? AND tenant_id=?", days, balance.get("id"),tenant());
        }
        jdbc.update("UPDATE hrm_leave_reservation SET state='RESERVED',employee_id=?,code=?,year=?,days=?,limited=?,leave_id=? WHERE id=?",
                employee,code,start.getYear(),days,limited ? 1 : 0,leaveId,row.get("id"));
    }

    @Transactional(rollbackFor=Exception.class)
    public void finish(String source,String key,boolean approve,String processId) {
        // Cancellation tombstones block late reserves after an RPC timeout/source rollback.
        if (!approve) jdbc.update("INSERT INTO hrm_leave_reservation(tenant_id,source,business_key,state) VALUES(?,?,?,'CANCELED') ON DUPLICATE KEY UPDATE id=id", tenant(),source,key);
        var row = reservation(source,key);
        String desired = approve ? "APPROVED" : "CANCELED";
        if (desired.equals(row.get("state"))) return;
        if (!"RESERVED".equals(row.get("state"))) throw new IllegalArgumentException("请假预占状态不允许更改");
        Long employee = ((Number) row.get("employee_id")).longValue();
        employees.validateEmployeeExistsForUpdate(employee);
        if (((Number)row.get("limited")).intValue() == 1) {
            var balance = lockBalance(employee,(String)row.get("code"),((Number)row.get("year")).intValue());
            BigDecimal days = decimal(row,"days");
            if (decimal(balance,"reserved").compareTo(days) < 0) throw new IllegalStateException("假期预占余额不一致");
            jdbc.update("UPDATE hrm_leave_balance SET reserved=reserved-?,used=used+? WHERE tenant_id=? AND id=?", days,approve ? days : BigDecimal.ZERO,tenant(),balance.get("id"));
        }
        jdbc.update("UPDATE hrm_leave_reservation SET state=? WHERE id=? AND tenant_id=?", desired,row.get("id"),tenant());
        if ("OA".equals(source)) {
            leaves.updateById(new HrmAttendanceLeaveDO().setId(((Number)row.get("leave_id")).longValue()).setProcessInstanceId(processId)
                    .setApprovalStatus((approve ? BpmProcessInstanceStatusEnum.APPROVE : BpmProcessInstanceStatusEnum.CANCEL).getStatus()).setApprovalTime(LocalDateTime.now()));
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public void grant(Long employee,String code,int year,BigDecimal entitlement) {
        employees.validateEmployeeExistsForUpdate(employee);
        var row = lockBalance(employee,code,year);
        if (entitlement.signum()<0 || entitlement.compareTo(decimal(row,"used").add(decimal(row,"reserved")))<0) throw new IllegalArgumentException("额度不能低于已使用与预占之和");
        jdbc.update("UPDATE hrm_leave_balance SET entitlement=? WHERE id=? AND tenant_id=?",entitlement,row.get("id"),tenant());
    }
    public List<Map<String,Object>> balances(Long employee,int year) {
        employees.validateEmployeeExists(employee);
        return jdbc.queryForList("SELECT code,year,entitlement,reserved,used,(entitlement-reserved-used) AS available FROM hrm_leave_balance WHERE tenant_id=? AND employee_id=? AND year=?",tenant(),employee,year);
    }
    public List<Map<String,Object>> policies() { initializePolicies(); return jdbc.queryForList("SELECT code,name,oa_type,limited FROM hrm_leave_policy WHERE tenant_id=? ORDER BY id",tenant()); }
    private void initializePolicies() {
        String[] names={"年假","事假","病假","婚假","产假","陪产假","丧假"};
        for(int i=0;i<names.length;i++) jdbc.update("INSERT IGNORE INTO hrm_leave_policy(tenant_id,code,name,oa_type,limited) VALUES(?,?,?,?,?)",tenant(),String.valueOf(i+1),names[i],i+1,i==0 ? 1 : 0);
    }
    private Map<String,Object> lockBalance(Long employee,String code,int year) {
        jdbc.update("INSERT INTO hrm_leave_balance(tenant_id,employee_id,code,year) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE id=id",tenant(),employee,code,year);
        return jdbc.queryForMap("SELECT * FROM hrm_leave_balance WHERE tenant_id=? AND employee_id=? AND code=? AND year=? FOR UPDATE",tenant(),employee,code,year);
    }
    private Map<String,Object> reservation(String source,String key) {
        var rows=jdbc.queryForList("SELECT * FROM hrm_leave_reservation WHERE tenant_id=? AND source=? AND business_key=? FOR UPDATE",tenant(),source,key);
        if(rows.isEmpty()) throw new IllegalArgumentException("请假预占不存在");
        return rows.getFirst();
    }
    private static BigDecimal decimal(Map<String,Object> row,String field) { return (BigDecimal)row.get(field); }
    private static Long tenant() { return TenantContextHolder.getRequiredTenantId(); }
}
