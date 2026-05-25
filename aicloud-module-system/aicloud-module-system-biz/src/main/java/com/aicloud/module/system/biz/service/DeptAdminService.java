package com.aicloud.module.system.biz.service;

import com.aicloud.module.system.biz.entity.AiDept;
import com.aicloud.module.system.biz.mapper.DeptMapper;
import com.aicloud.module.system.biz.model.dept.DeptResponse;
import com.aicloud.module.system.biz.model.dept.DeptSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class DeptAdminService {

    private final DeptMapper deptMapper;

    public DeptAdminService(DeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    public List<DeptResponse> tree(Long tenantId) {
        List<Map<String, Object>> rows = deptMapper.listDeptRows(tenantId);
        List<DeptResponse> all = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            DeptResponse item = new DeptResponse();
            item.setId(((Number) row.get("id")).longValue());
            item.setParentId(((Number) row.get("parent_id")).longValue());
            item.setName((String) row.get("name"));
            item.setLeaderUserId(row.get("leader_user_id") == null ? null : ((Number) row.get("leader_user_id")).longValue());
            item.setLeaderNickname((String) row.get("leader_nickname"));
            item.setSort(((Number) row.get("sort")).intValue());
            item.setStatus(((Number) row.get("status")).intValue());
            all.add(item);
        }
        Map<Long, DeptResponse> map = new HashMap<>(all.size());
        for (DeptResponse item : all) {
            map.put(item.getId(), item);
        }
        List<DeptResponse> roots = new ArrayList<>();
        for (DeptResponse item : all) {
            if (item.getParentId() == null || item.getParentId() == 0L || !map.containsKey(item.getParentId())) {
                roots.add(item);
            } else {
                map.get(item.getParentId()).getChildren().add(item);
            }
        }
        return roots;
    }

    @Transactional(rollbackFor = Exception.class)
    public DeptResponse save(Long tenantId, DeptSaveRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("部门名称不能为空");
        }
        AiDept dept = request.getId() == null ? new AiDept() : deptMapper.selectById(request.getId());
        if (dept == null) {
            throw new IllegalArgumentException("部门不存在");
        }
        if (request.getId() == null) {
            dept.setTenantId(tenantId);
            dept.setCreateTime(LocalDateTime.now());
        }
        dept.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        dept.setName(request.getName());
        dept.setLeaderUserId(request.getLeaderUserId());
        dept.setSort(request.getSort() == null ? 0 : request.getSort());
        dept.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        dept.setUpdateTime(LocalDateTime.now());
        if (request.getId() == null) {
            deptMapper.insert(dept);
        } else {
            deptMapper.updateById(dept);
        }
        return findOne(tenantId, dept.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long tenantId, Long id) {
        if (deptMapper.countChildren(tenantId, id) > 0) {
            throw new IllegalArgumentException("请先删除子部门");
        }
        if (deptMapper.countUsers(tenantId, id) > 0) {
            throw new IllegalArgumentException("部门下存在用户，不能删除");
        }
        deptMapper.delete(new LambdaQueryWrapper<AiDept>()
                .eq(AiDept::getTenantId, tenantId)
                .eq(AiDept::getId, id));
    }

    public DeptResponse findOne(Long tenantId, Long id) {
        for (DeptResponse item : flatten(tree(tenantId))) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    private List<DeptResponse> flatten(List<DeptResponse> list) {
        List<DeptResponse> all = new ArrayList<>();
        for (DeptResponse item : list) {
            all.add(item);
            all.addAll(flatten(item.getChildren()));
        }
        return all;
    }
}
