-- Existing installations: add the private attachment flag without replacing data.
USE aicloud;
SET @aicloud_private_file_ddl = IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
         AND table_name='infra_file_record' AND column_name='private_file'),
  'SELECT 1',
  'ALTER TABLE infra_file_record ADD COLUMN private_file bit(1) NOT NULL DEFAULT 0 COMMENT ''私有附件，仅限签名访问''');
PREPARE aicloud_private_file_stmt FROM @aicloud_private_file_ddl;
EXECUTE aicloud_private_file_stmt;
DEALLOCATE PREPARE aicloud_private_file_stmt;

-- Only OA attachments with matching upload ownership/size on gateway-backed storage.
UPDATE infra_file_record f
JOIN infra_file_config c ON c.id=f.config_id AND c.storage IN (1,10)
JOIN oa_file_node n ON n.url=f.url AND n.creator=f.creator AND n.size=f.size
SET f.private_file=1
WHERE f.deleted=0 AND n.deleted=0 AND n.type=1 AND f.private_file=0;
