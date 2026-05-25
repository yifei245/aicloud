package com.aicloud.module.system.biz.service;

import com.aicloud.module.system.biz.entity.AiPost;
import com.aicloud.module.system.biz.mapper.PostMapper;
import com.aicloud.module.system.biz.model.post.PostSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class PostAdminService {

    private final PostMapper postMapper;

    public PostAdminService(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public List<AiPost> list(Long tenantId, String keyword, Integer status) {
        return postMapper.selectList(new LambdaQueryWrapper<AiPost>()
                .eq(AiPost::getTenantId, tenantId)
                .and(StringUtils.hasText(keyword), w -> w.like(AiPost::getName, keyword).or().like(AiPost::getCode, keyword))
                .eq(status != null, AiPost::getStatus, status)
                .orderByAsc(AiPost::getSort, AiPost::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiPost save(Long tenantId, PostSaveRequest request) {
        if (!StringUtils.hasText(request.getCode()) || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("岗位编码和名称不能为空");
        }
        if (postMapper.countByCode(tenantId, request.getCode(), request.getId()) > 0) {
            throw new IllegalArgumentException("岗位编码已存在");
        }
        AiPost post = request.getId() == null ? new AiPost() : postMapper.selectById(request.getId());
        if (post == null) {
            throw new IllegalArgumentException("岗位不存在");
        }
        if (request.getId() == null) {
            post.setTenantId(tenantId);
            post.setCreateTime(LocalDateTime.now());
        }
        post.setCode(request.getCode());
        post.setName(request.getName());
        post.setSort(request.getSort() == null ? 0 : request.getSort());
        post.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        post.setUpdateTime(LocalDateTime.now());
        if (request.getId() == null) {
            postMapper.insert(post);
        } else {
            postMapper.updateById(post);
        }
        return post;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long tenantId, Long id) {
        if (postMapper.countUsers(tenantId, id) > 0) {
            throw new IllegalArgumentException("岗位下存在用户，不能删除");
        }
        postMapper.delete(new LambdaQueryWrapper<AiPost>()
                .eq(AiPost::getTenantId, tenantId)
                .eq(AiPost::getId, id));
    }
}
