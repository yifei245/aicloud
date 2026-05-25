package com.aicloud.module.system.biz.service;

import com.aicloud.module.system.biz.entity.AiRole;
import com.aicloud.module.system.biz.entity.AiRoleMenu;
import com.aicloud.module.system.biz.mapper.RoleMapper;
import com.aicloud.module.system.biz.mapper.RoleMenuMapper;
import com.aicloud.module.system.biz.model.role.RoleResponse;
import com.aicloud.module.system.biz.model.role.RoleSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class RoleAdminService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;

    public RoleAdminService(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    public List<RoleResponse> list(Long tenantId, String keyword, Integer status) {
        List<Map<String, Object>> rows = roleMapper.listRoleRows(tenantId, keyword, status);
        List<RoleResponse> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            RoleResponse item = new RoleResponse();
            item.setId(((Number) row.get("id")).longValue());
            item.setCode((String) row.get("code"));
            item.setName((String) row.get("name"));
            item.setDataScope((String) row.get("data_scope"));
            item.setSort(((Number) row.get("sort")).intValue());
            item.setStatus(((Number) row.get("status")).intValue());
            item.setUserCount(((Number) row.get("user_count")).intValue());
            item.setCreateTime((LocalDateTime) row.get("create_time"));
            item.setMenuIds(roleMapper.listMenuIds(tenantId, item.getId()));
            list.add(item);
        }
        return list;
    }

    public RoleResponse get(Long tenantId, Long id) {
        AiRole role = roleMapper.selectOne(new LambdaQueryWrapper<AiRole>()
                .eq(AiRole::getTenantId, tenantId)
                .eq(AiRole::getId, id));
        if (role == null) {
            return null;
        }
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setCode(role.getCode());
        response.setName(role.getName());
        response.setDataScope(role.getDataScope());
        response.setSort(role.getSort());
        response.setStatus(role.getStatus());
        response.setMenuIds(roleMapper.listMenuIds(tenantId, id));
        response.setCreateTime(role.getCreateTime());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleResponse create(Long tenantId, RoleSaveRequest request) {
        validate(tenantId, request, null);
        AiRole role = new AiRole();
        role.setTenantId(tenantId);
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDataScope(defaultIfBlank(request.getDataScope(), "ALL"));
        role.setSort(request.getSort() == null ? 0 : request.getSort());
        role.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);
        saveMenus(tenantId, role.getId(), request.getMenuIds());
        return get(tenantId, role.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleResponse update(Long tenantId, RoleSaveRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
        validate(tenantId, request, request.getId());
        AiRole role = roleMapper.selectOne(new LambdaQueryWrapper<AiRole>()
                .eq(AiRole::getTenantId, tenantId)
                .eq(AiRole::getId, request.getId()));
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDataScope(defaultIfBlank(request.getDataScope(), role.getDataScope()));
        role.setSort(request.getSort() == null ? role.getSort() : request.getSort());
        role.setStatus(request.getStatus() == null ? role.getStatus() : request.getStatus());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
        saveMenus(tenantId, role.getId(), request.getMenuIds());
        return get(tenantId, role.getId());
    }

    public void updateStatus(Long tenantId, Long id, Integer status) {
        roleMapper.update(null, new LambdaUpdateWrapper<AiRole>()
                .eq(AiRole::getTenantId, tenantId)
                .eq(AiRole::getId, id)
                .set(AiRole::getStatus, status)
                .set(AiRole::getUpdateTime, LocalDateTime.now()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long tenantId, Long id) {
        roleMenuMapper.delete(new LambdaQueryWrapper<AiRoleMenu>()
                .eq(AiRoleMenu::getTenantId, tenantId)
                .eq(AiRoleMenu::getRoleId, id));
        roleMapper.delete(new LambdaQueryWrapper<AiRole>()
                .eq(AiRole::getTenantId, tenantId)
                .eq(AiRole::getId, id));
    }

    private void saveMenus(Long tenantId, Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<AiRoleMenu>()
                .eq(AiRoleMenu::getTenantId, tenantId)
                .eq(AiRoleMenu::getRoleId, roleId));
        if (menuIds == null) {
            return;
        }
        for (Long menuId : menuIds) {
            if (menuId == null) {
                continue;
            }
            AiRoleMenu relation = new AiRoleMenu();
            relation.setTenantId(tenantId);
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuMapper.insert(relation);
        }
    }

    private void validate(Long tenantId, RoleSaveRequest request, Long excludeId) {
        if (!StringUtils.hasText(request.getCode())) {
            throw new IllegalArgumentException("角色编码不能为空");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("角色名称不能为空");
        }
        if (roleMapper.countByCode(tenantId, request.getCode(), excludeId) > 0) {
            throw new IllegalArgumentException("角色编码已存在");
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
