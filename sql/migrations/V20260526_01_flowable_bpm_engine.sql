USE aicloud;
SET NAMES utf8mb4;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_bpm_process_definition' AND COLUMN_NAME = 'engine_definition_id') = 0,
  'ALTER TABLE ai_bpm_process_definition ADD COLUMN engine_definition_id VARCHAR(128) NULL AFTER create_by',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_bpm_process_definition' AND COLUMN_NAME = 'deployment_id') = 0,
  'ALTER TABLE ai_bpm_process_definition ADD COLUMN deployment_id VARCHAR(128) NULL AFTER engine_definition_id',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_bpm_process_instance' AND COLUMN_NAME = 'engine_instance_id') = 0,
  'ALTER TABLE ai_bpm_process_instance ADD COLUMN engine_instance_id VARCHAR(128) NULL AFTER status',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_bpm_process_instance' AND COLUMN_NAME = 'engine_definition_id') = 0,
  'ALTER TABLE ai_bpm_process_instance ADD COLUMN engine_definition_id VARCHAR(128) NULL AFTER engine_instance_id',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_bpm_task' AND COLUMN_NAME = 'engine_task_id') = 0,
  'ALTER TABLE ai_bpm_task ADD COLUMN engine_task_id VARCHAR(128) NULL AFTER status',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_bpm_task' AND COLUMN_NAME = 'engine_instance_id') = 0,
  'ALTER TABLE ai_bpm_task ADD COLUMN engine_instance_id VARCHAR(128) NULL AFTER engine_task_id',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_bpm_process_definition' AND INDEX_NAME = 'idx_bpm_definition_engine') = 0,
  'CREATE INDEX idx_bpm_definition_engine ON ai_bpm_process_definition(engine_definition_id)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_bpm_process_instance' AND INDEX_NAME = 'idx_bpm_instance_engine') = 0,
  'CREATE INDEX idx_bpm_instance_engine ON ai_bpm_process_instance(engine_instance_id)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_bpm_task' AND INDEX_NAME = 'idx_bpm_task_engine') = 0,
  'CREATE INDEX idx_bpm_task_engine ON ai_bpm_task(engine_task_id)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
