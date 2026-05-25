package com.aicloud.gateway.service;

import com.aicloud.gateway.entity.AiAuditLog;
import com.aicloud.gateway.entity.AiAuditLogArchive;
import com.aicloud.gateway.mapper.AuditLogArchiveMapper;
import com.aicloud.gateway.mapper.AuditLogMapper;
import com.aicloud.gateway.model.AuditArchiveRunResponse;
import com.aicloud.gateway.model.AuditArchiveStatsResponse;
import com.aicloud.gateway.model.AuditLogItemResponse;
import com.aicloud.common.pojo.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class AuditArchiveService {

    private final AuditLogMapper auditLogMapper;
    private final AuditLogArchiveMapper auditLogArchiveMapper;

    public AuditArchiveService(AuditLogMapper auditLogMapper, AuditLogArchiveMapper auditLogArchiveMapper) {
        this.auditLogMapper = auditLogMapper;
        this.auditLogArchiveMapper = auditLogArchiveMapper;
    }

    public AuditArchiveStatsResponse stats(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(Math.max(retentionDays, 1));
        AuditArchiveStatsResponse response = new AuditArchiveStatsResponse();
        response.setCutoffTime(cutoff);
        response.setCurrentLogCount(auditLogMapper.countCurrent());
        response.setArchiveLogCount(auditLogMapper.countArchive());
        response.setPendingArchiveCount(auditLogMapper.countBefore(cutoff));
        response.setLatestArchivedTime(auditLogMapper.latestArchivedTime());
        return response;
    }

    public AuditArchiveRunResponse runOnce(int retentionDays, int batchSize) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(Math.max(retentionDays, 1));
        int safeBatch = Math.max(batchSize, 1);
        long totalBefore = auditLogMapper.countBefore(cutoff);

        int archived = 0;
        int deleted = 0;
        while (true) {
            int moved = auditLogMapper.archiveBatch(cutoff, safeBatch);
            if (moved <= 0) {
                break;
            }
            int removed = auditLogMapper.deleteBatch(cutoff, moved);
            archived += moved;
            deleted += removed;
            if (moved < safeBatch) {
                break;
            }
        }

        AuditArchiveRunResponse response = new AuditArchiveRunResponse();
        response.setCutoffTime(cutoff);
        response.setTotalBefore(totalBefore);
        response.setArchived(archived);
        response.setDeleted(deleted);
        return response;
    }

    public PageResponse<AuditLogItemResponse> queryLogs(boolean archived,
                                                        Long tenantId,
                                                        Long userId,
                                                        Integer success,
                                                        LocalDateTime startTime,
                                                        LocalDateTime endTime,
                                                        long pageNo,
                                                        long pageSize) {
        long safePageNo = Math.max(pageNo, 1);
        long safePageSize = Math.min(Math.max(pageSize, 1), 200);
        if (archived) {
            Page<AiAuditLogArchive> page = new Page<>(safePageNo, safePageSize);
            LambdaQueryWrapper<AiAuditLogArchive> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(tenantId != null, AiAuditLogArchive::getTenantId, tenantId)
                    .eq(userId != null, AiAuditLogArchive::getUserId, userId)
                    .eq(success != null, AiAuditLogArchive::getSuccess, success)
                    .ge(startTime != null, AiAuditLogArchive::getCreateTime, startTime)
                    .le(endTime != null, AiAuditLogArchive::getCreateTime, endTime)
                    .orderByDesc(AiAuditLogArchive::getId);
            Page<AiAuditLogArchive> result = auditLogArchiveMapper.selectPage(page, wrapper);
            List<AuditLogItemResponse> list = result.getRecords().stream().map(this::fromArchive).toList();
            return toPageResponse(result.getTotal(), result.getCurrent(), result.getSize(), list);
        }

        Page<AiAuditLog> page = new Page<>(safePageNo, safePageSize);
        LambdaQueryWrapper<AiAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(tenantId != null, AiAuditLog::getTenantId, tenantId)
                .eq(userId != null, AiAuditLog::getUserId, userId)
                .eq(success != null, AiAuditLog::getSuccess, success)
                .ge(startTime != null, AiAuditLog::getCreateTime, startTime)
                .le(endTime != null, AiAuditLog::getCreateTime, endTime)
                .orderByDesc(AiAuditLog::getId);
        Page<AiAuditLog> result = auditLogMapper.selectPage(page, wrapper);
        List<AuditLogItemResponse> list = result.getRecords().stream().map(this::fromCurrent).toList();
        return toPageResponse(result.getTotal(), result.getCurrent(), result.getSize(), list);
    }

    public List<AuditLogItemResponse> queryLogsForExport(boolean archived,
                                                         Long tenantId,
                                                         Long userId,
                                                         Integer success,
                                                         LocalDateTime startTime,
                                                         LocalDateTime endTime,
                                                         int maxRows) {
        int safeMaxRows = Math.min(Math.max(maxRows, 1), 20000);
        if (archived) {
            LambdaQueryWrapper<AiAuditLogArchive> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(tenantId != null, AiAuditLogArchive::getTenantId, tenantId)
                    .eq(userId != null, AiAuditLogArchive::getUserId, userId)
                    .eq(success != null, AiAuditLogArchive::getSuccess, success)
                    .ge(startTime != null, AiAuditLogArchive::getCreateTime, startTime)
                    .le(endTime != null, AiAuditLogArchive::getCreateTime, endTime)
                    .orderByDesc(AiAuditLogArchive::getId)
                    .last("LIMIT " + safeMaxRows);
            return auditLogArchiveMapper.selectList(wrapper).stream().map(this::fromArchive).toList();
        }
        LambdaQueryWrapper<AiAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(tenantId != null, AiAuditLog::getTenantId, tenantId)
                .eq(userId != null, AiAuditLog::getUserId, userId)
                .eq(success != null, AiAuditLog::getSuccess, success)
                .ge(startTime != null, AiAuditLog::getCreateTime, startTime)
                .le(endTime != null, AiAuditLog::getCreateTime, endTime)
                .orderByDesc(AiAuditLog::getId)
                .last("LIMIT " + safeMaxRows);
        return auditLogMapper.selectList(wrapper).stream().map(this::fromCurrent).toList();
    }

    public byte[] buildCsv(List<AuditLogItemResponse> logs) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,sourceId,tenantId,userId,username,module,operation,requestUri,requestMethod,requestIp,success,errorMsg,createTime,archivedTime\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AuditLogItemResponse item : logs) {
            csv.append(toCsv(item.getId())).append(',')
                    .append(toCsv(item.getSourceId())).append(',')
                    .append(toCsv(item.getTenantId())).append(',')
                    .append(toCsv(item.getUserId())).append(',')
                    .append(toCsv(item.getUsername())).append(',')
                    .append(toCsv(item.getModule())).append(',')
                    .append(toCsv(item.getOperation())).append(',')
                    .append(toCsv(item.getRequestUri())).append(',')
                    .append(toCsv(item.getRequestMethod())).append(',')
                    .append(toCsv(item.getRequestIp())).append(',')
                    .append(toCsv(item.getSuccess())).append(',')
                    .append(toCsv(item.getErrorMsg())).append(',')
                    .append(toCsv(item.getCreateTime() == null ? null : item.getCreateTime().format(formatter))).append(',')
                    .append(toCsv(item.getArchivedTime() == null ? null : item.getArchivedTime().format(formatter)))
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private PageResponse<AuditLogItemResponse> toPageResponse(long total, long pageNo, long pageSize, List<AuditLogItemResponse> list) {
        PageResponse<AuditLogItemResponse> response = new PageResponse<>();
        response.setTotal(total);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setList(list);
        return response;
    }

    private AuditLogItemResponse fromCurrent(AiAuditLog log) {
        AuditLogItemResponse item = new AuditLogItemResponse();
        item.setId(log.getId());
        item.setTenantId(log.getTenantId());
        item.setUserId(log.getUserId());
        item.setUsername(log.getUsername());
        item.setModule(log.getModule());
        item.setOperation(log.getOperation());
        item.setRequestUri(log.getRequestUri());
        item.setRequestMethod(log.getRequestMethod());
        item.setRequestIp(log.getRequestIp());
        item.setSuccess(log.getSuccess());
        item.setErrorMsg(log.getErrorMsg());
        item.setCreateTime(log.getCreateTime());
        return item;
    }

    private AuditLogItemResponse fromArchive(AiAuditLogArchive log) {
        AuditLogItemResponse item = new AuditLogItemResponse();
        item.setId(log.getId());
        item.setSourceId(log.getSourceId());
        item.setTenantId(log.getTenantId());
        item.setUserId(log.getUserId());
        item.setUsername(log.getUsername());
        item.setModule(log.getModule());
        item.setOperation(log.getOperation());
        item.setRequestUri(log.getRequestUri());
        item.setRequestMethod(log.getRequestMethod());
        item.setRequestIp(log.getRequestIp());
        item.setSuccess(log.getSuccess());
        item.setErrorMsg(log.getErrorMsg());
        item.setCreateTime(log.getCreateTime());
        item.setArchivedTime(log.getArchivedTime());
        return item;
    }

    private String toCsv(Object val) {
        if (val == null) {
            return "";
        }
        String raw = String.valueOf(val);
        if (raw.contains(",") || raw.contains("\"") || raw.contains("\n")) {
            return "\"" + raw.replace("\"", "\"\"") + "\"";
        }
        return raw;
    }
}
