package com.aicloud.module.system.biz.service;

import com.aicloud.module.system.biz.entity.AiUser;
import com.aicloud.module.system.biz.entity.AiUserDeptPost;
import com.aicloud.module.system.biz.entity.AiUserRole;
import com.aicloud.module.system.biz.mapper.UserDeptPostMapper;
import com.aicloud.module.system.biz.mapper.UserMapper;
import com.aicloud.module.system.biz.mapper.UserRoleMapper;
import com.aicloud.common.pojo.PageResponse;
import com.aicloud.module.system.biz.model.user.UserResponse;
import com.aicloud.module.system.biz.model.user.UserSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class UserAdminService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserDeptPostMapper userDeptPostMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserAdminService(UserMapper userMapper, UserRoleMapper userRoleMapper, UserDeptPostMapper userDeptPostMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.userDeptPostMapper = userDeptPostMapper;
    }

    public PageResponse<UserResponse> list(Long tenantId, String keyword, Integer status, Long deptId, long pageNo, long pageSize) {
        List<Map<String, Object>> rows = userMapper.listUserRows(tenantId, keyword, status, deptId);
        List<UserResponse> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            UserResponse item = new UserResponse();
            item.setId(getLong(row.get("id")));
            item.setTenantId(getLong(row.get("tenant_id")));
            item.setUsername((String) row.get("username"));
            item.setNickname((String) row.get("nickname"));
            item.setMobile((String) row.get("mobile"));
            item.setEmail((String) row.get("email"));
            item.setUserType((String) row.get("user_type"));
            item.setStatus(getInt(row.get("status")));
            item.setDeptId(getLong(row.get("dept_id")));
            item.setDeptName((String) row.get("dept_name"));
            item.setLastLoginTime((LocalDateTime) row.get("last_login_time"));
            item.setCreateTime((LocalDateTime) row.get("create_time"));
            item.setRoleIds(userMapper.listRoleIds(tenantId, item.getId()));
            item.setRoleCodes(userMapper.listRoleCodes(tenantId, item.getId()));
            item.setPostIds(userMapper.listPostIds(tenantId, item.getId()));
            item.setPostNames(userMapper.listPostNames(tenantId, item.getId()));
            list.add(item);
        }
        long safePageNo = Math.max(pageNo, 1);
        long safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int fromIndex = (int) Math.min((safePageNo - 1) * safePageSize, list.size());
        int toIndex = (int) Math.min(fromIndex + safePageSize, list.size());
        return new PageResponse<>(list.size(), safePageNo, safePageSize, list.subList(fromIndex, toIndex));
    }

    public UserResponse get(Long tenantId, Long id) {
        AiUser user = userMapper.selectOne(new LambdaQueryWrapper<AiUser>()
                .eq(AiUser::getTenantId, tenantId)
                .eq(AiUser::getId, id));
        if (user == null) {
            return null;
        }
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setTenantId(user.getTenantId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setMobile(user.getMobile());
        response.setEmail(user.getEmail());
        response.setUserType(user.getUserType());
        response.setStatus(user.getStatus());
        response.setDeptId(userMapper.findPrimaryDeptId(tenantId, id));
        response.setRoleIds(userMapper.listRoleIds(tenantId, id));
        response.setRoleCodes(userMapper.listRoleCodes(tenantId, id));
        response.setPostIds(userMapper.listPostIds(tenantId, id));
        response.setPostNames(userMapper.listPostNames(tenantId, id));
        response.setLastLoginTime(user.getLastLoginTime());
        response.setCreateTime(user.getCreateTime());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponse create(Long tenantId, UserSaveRequest request) {
        validateSaveRequest(tenantId, request, null);
        AiUser user = new AiUser();
        user.setTenantId(tenantId);
        user.setUsername(request.getUsername());
        user.setPassword(encodePassword(defaultIfBlank(request.getPassword(), "123456")));
        user.setNickname(request.getNickname());
        user.setMobile(request.getMobile());
        user.setEmail(request.getEmail());
        user.setUserType(defaultIfBlank(request.getUserType(), "ADMIN"));
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        saveRelations(tenantId, user.getId(), request.getDeptId(), request.getPostIds(), request.getRoleIds());
        return get(tenantId, user.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public UserResponse update(Long tenantId, UserSaveRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        AiUser existing = userMapper.selectOne(new LambdaQueryWrapper<AiUser>()
                .eq(AiUser::getTenantId, tenantId)
                .eq(AiUser::getId, request.getId()));
        if (existing == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        validateSaveRequest(tenantId, request, request.getId());
        existing.setNickname(request.getNickname());
        existing.setMobile(request.getMobile());
        existing.setEmail(request.getEmail());
        existing.setUserType(defaultIfBlank(request.getUserType(), existing.getUserType()));
        existing.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(existing);
        saveRelations(tenantId, existing.getId(), request.getDeptId(), request.getPostIds(), request.getRoleIds());
        return get(tenantId, existing.getId());
    }

    public void updateStatus(Long tenantId, Long id, Integer status) {
        userMapper.update(null, new LambdaUpdateWrapper<AiUser>()
                .eq(AiUser::getTenantId, tenantId)
                .eq(AiUser::getId, id)
                .set(AiUser::getStatus, status)
                .set(AiUser::getUpdateTime, LocalDateTime.now()));
    }

    public void resetPassword(Long tenantId, Long id, String password) {
        userMapper.update(null, new LambdaUpdateWrapper<AiUser>()
                .eq(AiUser::getTenantId, tenantId)
                .eq(AiUser::getId, id)
                .set(AiUser::getPassword, encodePassword(defaultIfBlank(password, "123456")))
                .set(AiUser::getUpdateTime, LocalDateTime.now()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long tenantId, Long id) {
        userRoleMapper.delete(new LambdaQueryWrapper<AiUserRole>()
                .eq(AiUserRole::getTenantId, tenantId)
                .eq(AiUserRole::getUserId, id));
        userDeptPostMapper.delete(new LambdaQueryWrapper<AiUserDeptPost>()
                .eq(AiUserDeptPost::getTenantId, tenantId)
                .eq(AiUserDeptPost::getUserId, id));
        userMapper.delete(new LambdaQueryWrapper<AiUser>()
                .eq(AiUser::getTenantId, tenantId)
                .eq(AiUser::getId, id));
    }

    private void validateSaveRequest(Long tenantId, UserSaveRequest request, Long excludeId) {
        if (!StringUtils.hasText(request.getUsername()) && excludeId == null) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getNickname())) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        if (excludeId == null || StringUtils.hasText(request.getUsername())) {
            long count = userMapper.countByUsername(tenantId, request.getUsername(), excludeId);
            if (count > 0) {
                throw new IllegalArgumentException("用户名已存在");
            }
        }
    }

    private void saveRelations(Long tenantId, Long userId, Long deptId, List<Long> postIds, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<AiUserRole>()
                .eq(AiUserRole::getTenantId, tenantId)
                .eq(AiUserRole::getUserId, userId));
        userDeptPostMapper.delete(new LambdaQueryWrapper<AiUserDeptPost>()
                .eq(AiUserDeptPost::getTenantId, tenantId)
                .eq(AiUserDeptPost::getUserId, userId));
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                if (roleId == null) {
                    continue;
                }
                AiUserRole userRole = new AiUserRole();
                userRole.setTenantId(tenantId);
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        List<Long> safePostIds = postIds == null || postIds.isEmpty() ? Collections.singletonList(null) : postIds;
        for (Long postId : safePostIds) {
            AiUserDeptPost relation = new AiUserDeptPost();
            relation.setTenantId(tenantId);
            relation.setUserId(userId);
            relation.setDeptId(deptId == null ? tenantId : deptId);
            relation.setPostId(postId);
            userDeptPostMapper.insert(relation);
        }
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private Long getLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private Integer getInt(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }
}
