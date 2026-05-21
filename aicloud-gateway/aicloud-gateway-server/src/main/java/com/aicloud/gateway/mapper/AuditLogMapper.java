package com.aicloud.gateway.mapper;

import com.aicloud.gateway.entity.AiAuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface AuditLogMapper extends BaseMapper<AiAuditLog> {

    @Select("""
            SELECT COUNT(1)
            FROM ai_audit_log
            """)
    long countCurrent();

    @Select("""
            SELECT COUNT(1)
            FROM ai_audit_log_archive
            """)
    long countArchive();

    @Select("""
            SELECT COUNT(1)
            FROM ai_audit_log
            WHERE create_time < #{cutoff}
            """)
    long countBefore(@Param("cutoff") LocalDateTime cutoff);

    @Select("""
            SELECT MAX(archived_time)
            FROM ai_audit_log_archive
            """)
    LocalDateTime latestArchivedTime();

    @Insert("""
            INSERT IGNORE INTO ai_audit_log_archive
            (source_id, tenant_id, user_id, username, module, operation, request_uri, request_method, request_ip, success, error_msg, create_time, archived_time)
            SELECT id, tenant_id, user_id, username, module, operation, request_uri, request_method, request_ip, success, error_msg, create_time, NOW()
            FROM ai_audit_log
            WHERE create_time < #{cutoff}
            ORDER BY id
            LIMIT #{batchSize}
            """)
    int archiveBatch(@Param("cutoff") LocalDateTime cutoff, @Param("batchSize") int batchSize);

    @Delete("""
            DELETE FROM ai_audit_log
            WHERE id IN (
              SELECT id FROM (
                SELECT id
                FROM ai_audit_log
                WHERE create_time < #{cutoff}
                ORDER BY id
                LIMIT #{batchSize}
              ) t
            )
            """)
    int deleteBatch(@Param("cutoff") LocalDateTime cutoff, @Param("batchSize") int batchSize);
}
