package com.aicloud.module.infra.biz.controller;

import com.aicloud.module.infra.biz.entity.AiInfraConfig;
import com.aicloud.module.infra.biz.entity.AiInfraFile;
import com.aicloud.module.infra.biz.entity.AiInfraJob;
import com.aicloud.module.infra.biz.entity.AiInfraNotice;
import com.aicloud.module.infra.biz.model.ApiResponse;
import com.aicloud.module.infra.biz.model.InfraConfigSaveRequest;
import com.aicloud.module.infra.biz.service.InfraConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "基础设施")
@RestController
@RequestMapping("/infra")
public class InfraConfigController {

    private final InfraConfigService infraConfigService;

    public InfraConfigController(InfraConfigService infraConfigService) {
        this.infraConfigService = infraConfigService;
    }

    @Operation(summary = "基础设施状态")
    @GetMapping("/config/status")
    public ApiResponse<Map<String, Object>> status(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(infraConfigService.status(tenantId));
    }

    @Operation(summary = "配置列表")
    @GetMapping("/config/list")
    public ApiResponse<List<AiInfraConfig>> list(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(infraConfigService.list(tenantId, keyword, status));
    }

    @Operation(summary = "配置详情")
    @GetMapping("/config/{id}")
    public ApiResponse<AiInfraConfig> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(infraConfigService.get(id));
    }

    @Operation(summary = "创建或更新配置")
    @PostMapping("/config/save")
    public ApiResponse<AiInfraConfig> save(@Valid @RequestBody InfraConfigSaveRequest request) {
        return ApiResponse.ok(infraConfigService.save(request));
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/config/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Long id) {
        infraConfigService.delete(id);
        return ApiResponse.ok(Boolean.TRUE);
    }

    @Operation(summary = "文件列表")
    @GetMapping("/file/list")
    public ApiResponse<List<AiInfraFile>> fileList(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResponse.ok(infraConfigService.fileList(tenantId, keyword));
    }

    @Operation(summary = "保存文件记录")
    @PostMapping("/file/save")
    public ApiResponse<AiInfraFile> saveFile(@RequestBody AiInfraFile body) {
        return ApiResponse.ok(infraConfigService.saveFile(body));
    }

    @Operation(summary = "删除文件记录")
    @DeleteMapping("/file/{id}")
    public ApiResponse<Boolean> deleteFile(@PathVariable("id") Long id) {
        infraConfigService.deleteFile(id);
        return ApiResponse.ok(Boolean.TRUE);
    }

    @Operation(summary = "任务列表")
    @GetMapping("/job/list")
    public ApiResponse<List<AiInfraJob>> jobList(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(infraConfigService.jobList(tenantId, status));
    }

    @Operation(summary = "保存任务")
    @PostMapping("/job/save")
    public ApiResponse<AiInfraJob> saveJob(@RequestBody AiInfraJob body) {
        return ApiResponse.ok(infraConfigService.saveJob(body));
    }

    @Operation(summary = "手动执行任务")
    @PostMapping("/job/run")
    public ApiResponse<AiInfraJob> runJob(@RequestParam Long id) {
        return ApiResponse.ok(infraConfigService.runJob(id));
    }

    @Operation(summary = "通知列表")
    @GetMapping("/notice/list")
    public ApiResponse<List<AiInfraNotice>> noticeList(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(infraConfigService.noticeList(tenantId, status));
    }

    @Operation(summary = "保存通知")
    @PostMapping("/notice/save")
    public ApiResponse<AiInfraNotice> saveNotice(@RequestBody AiInfraNotice body) {
        return ApiResponse.ok(infraConfigService.saveNotice(body));
    }

    @Operation(summary = "发布通知")
    @PostMapping("/notice/publish")
    public ApiResponse<AiInfraNotice> publishNotice(@RequestParam Long id) {
        return ApiResponse.ok(infraConfigService.publishNotice(id));
    }
}
