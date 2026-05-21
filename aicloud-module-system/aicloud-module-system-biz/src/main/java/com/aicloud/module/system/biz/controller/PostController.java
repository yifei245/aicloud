package com.aicloud.module.system.biz.controller;

import com.aicloud.module.system.biz.annotation.RequirePermission;
import com.aicloud.module.system.biz.entity.AiPost;
import com.aicloud.module.system.biz.model.ApiResponse;
import com.aicloud.module.system.biz.model.post.PostSaveRequest;
import com.aicloud.module.system.biz.service.PostAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统岗位")
@RestController
@RequestMapping("/system/post")
public class PostController {

    private final PostAdminService postAdminService;

    public PostController(PostAdminService postAdminService) {
        this.postAdminService = postAdminService;
    }

    @Operation(summary = "岗位列表")
    @RequirePermission("system:post:query")
    @GetMapping("/list")
    public ApiResponse<List<AiPost>> list(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                          @RequestParam(name = "keyword", required = false) String keyword,
                                          @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(postAdminService.list(parseTenantId(tenantIdHeader), keyword, status));
    }

    @Operation(summary = "创建岗位")
    @RequirePermission("system:post:create")
    @PostMapping("/create")
    public ApiResponse<AiPost> create(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                      @RequestBody PostSaveRequest request) {
        return ApiResponse.ok(postAdminService.save(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "更新岗位")
    @RequirePermission("system:post:update")
    @PutMapping("/update")
    public ApiResponse<AiPost> update(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                      @RequestBody PostSaveRequest request) {
        return ApiResponse.ok(postAdminService.save(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "删除岗位")
    @RequirePermission("system:post:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                       @PathVariable("id") Long id) {
        postAdminService.delete(parseTenantId(tenantIdHeader), id);
        return ApiResponse.ok(true);
    }

    private Long parseTenantId(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return 1L;
        }
        return Long.parseLong(tenantIdHeader);
    }
}
