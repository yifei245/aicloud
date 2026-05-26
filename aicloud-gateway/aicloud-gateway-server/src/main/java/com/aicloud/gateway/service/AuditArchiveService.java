package com.aicloud.gateway.service;

import com.aicloud.common.pojo.PageResponse;
import com.aicloud.gateway.model.AuditArchiveRunResponse;
import com.aicloud.gateway.model.AuditArchiveStatsResponse;
import com.aicloud.gateway.model.AuditLogItemResponse;
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

    private static final String CSV_COMMA = ",";
    private static final String CSV_QUOTE = "\"";
    private static final String LINE_SEPARATOR = "\n";

    public AuditArchiveStatsResponse stats(int retentionDays) {
        AuditArchiveStatsResponse response = new AuditArchiveStatsResponse();
        response.setCutoffTime(LocalDateTime.now().minusDays(Math.max(retentionDays, 1)));
        response.setCurrentLogCount(0L);
        response.setArchiveLogCount(0L);
        response.setPendingArchiveCount(0L);
        response.setLatestArchivedTime(null);
        return response;
    }

    public AuditArchiveRunResponse runOnce(int retentionDays, int batchSize) {
        AuditArchiveRunResponse response = new AuditArchiveRunResponse();
        response.setCutoffTime(LocalDateTime.now().minusDays(Math.max(retentionDays, 1)));
        response.setTotalBefore(0L);
        response.setArchived(0);
        response.setDeleted(0);
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
        PageResponse<AuditLogItemResponse> response = new PageResponse<>();
        response.setTotal(0L);
        response.setPageNo(Math.max(pageNo, 1));
        response.setPageSize(Math.min(Math.max(pageSize, 1), 200));
        response.setList(List.of());
        return response;
    }

    public List<AuditLogItemResponse> queryLogsForExport(boolean archived,
                                                         Long tenantId,
                                                         Long userId,
                                                         Integer success,
                                                         LocalDateTime startTime,
                                                         LocalDateTime endTime,
                                                         int maxRows) {
        return List.of();
    }

    public byte[] buildCsv(List<AuditLogItemResponse> logs) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,sourceId,tenantId,userId,username,module,operation,requestUri,requestMethod,requestIp,success,errorMsg,createTime,archivedTime")
                .append(LINE_SEPARATOR);
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
                    .append(LINE_SEPARATOR);
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String toCsv(Object val) {
        if (val == null) {
            return "";
        }
        String raw = String.valueOf(val);
        if (raw.contains(CSV_COMMA) || raw.contains(CSV_QUOTE) || raw.contains(LINE_SEPARATOR)) {
            return CSV_QUOTE + raw.replace(CSV_QUOTE, CSV_QUOTE + CSV_QUOTE) + CSV_QUOTE;
        }
        return raw;
    }
}
