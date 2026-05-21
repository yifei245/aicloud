package com.aicloud.module.infra.biz.service;

import com.aicloud.module.infra.biz.entity.AiInfraConfig;
import com.aicloud.module.infra.biz.entity.AiInfraFile;
import com.aicloud.module.infra.biz.entity.AiInfraJob;
import com.aicloud.module.infra.biz.entity.AiInfraNotice;
import com.aicloud.module.infra.biz.mapper.InfraConfigMapper;
import com.aicloud.module.infra.biz.mapper.InfraFileMapper;
import com.aicloud.module.infra.biz.mapper.InfraJobMapper;
import com.aicloud.module.infra.biz.mapper.InfraNoticeMapper;
import com.aicloud.module.infra.biz.model.InfraConfigSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class InfraConfigService {

    private final InfraConfigMapper infraConfigMapper;
    private final InfraFileMapper infraFileMapper;
    private final InfraJobMapper infraJobMapper;
    private final InfraNoticeMapper infraNoticeMapper;

    public InfraConfigService(InfraConfigMapper infraConfigMapper, InfraFileMapper infraFileMapper,
            InfraJobMapper infraJobMapper, InfraNoticeMapper infraNoticeMapper) {
        this.infraConfigMapper = infraConfigMapper;
        this.infraFileMapper = infraFileMapper;
        this.infraJobMapper = infraJobMapper;
        this.infraNoticeMapper = infraNoticeMapper;
    }

    public Map<String, Object> status(Long tenantId) {
        Map<String, Object> data = new HashMap<>();
        data.put("module", "infra");
        data.put("status", "UP");
        data.put("tenantId", tenantId);
        data.put("configTotalCount", infraConfigMapper.selectCount(new LambdaQueryWrapper<AiInfraConfig>().eq(AiInfraConfig::getTenantId, tenantId)));
        data.put("fileCount", infraFileMapper.selectCount(new LambdaQueryWrapper<AiInfraFile>().eq(AiInfraFile::getTenantId, tenantId)));
        data.put("jobCount", infraJobMapper.selectCount(new LambdaQueryWrapper<AiInfraJob>().eq(AiInfraJob::getTenantId, tenantId)));
        data.put("noticeCount", infraNoticeMapper.selectCount(new LambdaQueryWrapper<AiInfraNotice>().eq(AiInfraNotice::getTenantId, tenantId)));
        data.put("now", LocalDateTime.now());
        return data;
    }

    public List<AiInfraConfig> list(Long tenantId, String keyword, Integer status) {
        return infraConfigMapper.selectList(new LambdaQueryWrapper<AiInfraConfig>()
                .eq(AiInfraConfig::getTenantId, tenantId)
                .eq(status != null, AiInfraConfig::getStatus, status)
                .and(StringUtils.hasText(keyword), query -> query.like(AiInfraConfig::getConfigKey, keyword).or().like(AiInfraConfig::getConfigName, keyword))
                .orderByDesc(AiInfraConfig::getId));
    }

    public AiInfraConfig get(Long id) {
        AiInfraConfig config = infraConfigMapper.selectById(id);
        if (config == null) throw new IllegalArgumentException("配置不存在");
        return config;
    }

    public AiInfraConfig save(InfraConfigSaveRequest request) {
        AiInfraConfig config = request.getId() == null ? new AiInfraConfig() : get(request.getId());
        config.setTenantId(request.getTenantId());
        config.setConfigKey(request.getConfigKey());
        config.setConfigName(request.getConfigName());
        config.setConfigValue(request.getConfigValue());
        config.setConfigType(StringUtils.hasText(request.getConfigType()) ? request.getConfigType() : "SYSTEM");
        config.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        config.setRemark(request.getRemark());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getId() == null) {
            config.setCreateTime(LocalDateTime.now());
            infraConfigMapper.insert(config);
        } else {
            infraConfigMapper.updateById(config);
        }
        return config;
    }

    public void delete(Long id) {
        if (infraConfigMapper.deleteById(id) == 0) throw new IllegalArgumentException("配置不存在");
    }

    public List<AiInfraFile> fileList(Long tenantId, String keyword) {
        return infraFileMapper.selectList(new LambdaQueryWrapper<AiInfraFile>()
                .eq(AiInfraFile::getTenantId, tenantId)
                .like(StringUtils.hasText(keyword), AiInfraFile::getFileName, keyword)
                .orderByDesc(AiInfraFile::getId));
    }

    public AiInfraFile saveFile(AiInfraFile file) {
        file.setUpdateTime(LocalDateTime.now());
        if (file.getStatus() == null) file.setStatus(1);
        if (!StringUtils.hasText(file.getStorage())) file.setStorage("LOCAL");
        if (file.getId() == null) {
            file.setCreateTime(LocalDateTime.now());
            infraFileMapper.insert(file);
        } else {
            infraFileMapper.updateById(file);
        }
        return file;
    }

    public void deleteFile(Long id) {
        if (infraFileMapper.deleteById(id) == 0) throw new IllegalArgumentException("文件不存在");
    }

    public List<AiInfraJob> jobList(Long tenantId, Integer status) {
        return infraJobMapper.selectList(new LambdaQueryWrapper<AiInfraJob>()
                .eq(AiInfraJob::getTenantId, tenantId)
                .eq(status != null, AiInfraJob::getStatus, status)
                .orderByDesc(AiInfraJob::getId));
    }

    public AiInfraJob saveJob(AiInfraJob job) {
        job.setUpdateTime(LocalDateTime.now());
        if (job.getStatus() == null) job.setStatus(1);
        if (job.getId() == null) {
            job.setCreateTime(LocalDateTime.now());
            infraJobMapper.insert(job);
        } else {
            infraJobMapper.updateById(job);
        }
        return job;
    }

    public AiInfraJob runJob(Long id) {
        AiInfraJob job = infraJobMapper.selectById(id);
        if (job == null) throw new IllegalArgumentException("任务不存在");
        job.setLastRunTime(LocalDateTime.now());
        job.setNextRunTime(LocalDateTime.now().plusMinutes(5));
        job.setUpdateTime(LocalDateTime.now());
        infraJobMapper.updateById(job);
        return job;
    }

    public List<AiInfraNotice> noticeList(Long tenantId, String status) {
        return infraNoticeMapper.selectList(new LambdaQueryWrapper<AiInfraNotice>()
                .eq(AiInfraNotice::getTenantId, tenantId)
                .eq(StringUtils.hasText(status), AiInfraNotice::getStatus, status)
                .orderByDesc(AiInfraNotice::getId));
    }

    public AiInfraNotice saveNotice(AiInfraNotice notice) {
        notice.setUpdateTime(LocalDateTime.now());
        if (!StringUtils.hasText(notice.getStatus())) notice.setStatus("DRAFT");
        if (!StringUtils.hasText(notice.getReceiverType())) notice.setReceiverType("ALL");
        if (notice.getId() == null) {
            notice.setCreateTime(LocalDateTime.now());
            infraNoticeMapper.insert(notice);
        } else {
            infraNoticeMapper.updateById(notice);
        }
        return notice;
    }

    public AiInfraNotice publishNotice(Long id) {
        AiInfraNotice notice = infraNoticeMapper.selectById(id);
        if (notice == null) throw new IllegalArgumentException("通知不存在");
        notice.setStatus("PUBLISHED");
        notice.setPublishTime(LocalDateTime.now());
        notice.setUpdateTime(LocalDateTime.now());
        infraNoticeMapper.updateById(notice);
        return notice;
    }
}
