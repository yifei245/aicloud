USE aicloud;
CREATE TABLE IF NOT EXISTS `crm_erp_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `kind` varchar(16) NOT NULL,
  `crm_id` bigint NOT NULL,
  `erp_id` bigint NOT NULL,
  `source_unit` int DEFAULT NULL,
  `unit` varchar(64) DEFAULT NULL,
  UNIQUE KEY uk_mapping(tenant_id,kind,crm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `crm_erp_contract_sale` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `contract_id` bigint NOT NULL,
  `sale_order_id` bigint NOT NULL,
  UNIQUE KEY uk_contract(tenant_id,contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `hrm_leave_policy` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `code` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `oa_type` int DEFAULT NULL,
  `limited` tinyint NOT NULL DEFAULT 0,
  UNIQUE KEY uk_policy(tenant_id,code),
  UNIQUE KEY uk_oa_type(tenant_id,oa_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `hrm_leave_balance` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `employee_id` bigint NOT NULL,
  `code` varchar(64) NOT NULL,
  `year` int NOT NULL,
  `entitlement` decimal(6,2) NOT NULL DEFAULT 0,
  `reserved` decimal(6,2) NOT NULL DEFAULT 0,
  `used` decimal(6,2) NOT NULL DEFAULT 0,
  UNIQUE KEY uk_balance(tenant_id,employee_id,code,year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `hrm_leave_reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `source` varchar(16) NOT NULL,
  `business_key` varchar(160) NOT NULL,
  `payload_hash` varchar(64) DEFAULT NULL,
  `employee_id` bigint DEFAULT NULL,
  `code` varchar(64) DEFAULT NULL,
  `year` int DEFAULT NULL,
  `days` decimal(6,2) DEFAULT NULL,
  `limited` tinyint NOT NULL DEFAULT 0,
  `leave_id` bigint DEFAULT NULL,
  `state` varchar(16) NOT NULL,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_reservation(tenant_id,source,business_key),
  KEY idx_pending(source,state,create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `integration_outbox` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `producer` varchar(64) NOT NULL,
  `event_type` varchar(100) NOT NULL,
  `business_key` varchar(200) NOT NULL,
  `aggregate_key` varchar(200) NOT NULL DEFAULT '',
  `payload` longtext NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `attempts` int NOT NULL DEFAULT 0,
  `next_attempt_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `lease_until` datetime(3) DEFAULT NULL,
  `lock_token` varchar(36) DEFAULT NULL,
  `last_error` varchar(200) DEFAULT NULL,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_source (tenant_id,producer,business_key),
  KEY idx_pending (producer,status,next_attempt_time),
  KEY idx_aggregate (tenant_id,producer,aggregate_key,id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `fms_business_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `source` varchar(32) NOT NULL,
  `account_set_id` bigint NOT NULL,
  `voucher_word_id` bigint NOT NULL,
  `debit_subject_id` bigint NOT NULL,
  `credit_subject_id` bigint NOT NULL,
  `posting_user_id` bigint NOT NULL,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_rule (tenant_id,source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `fms_business_source` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `source` varchar(32) NOT NULL,
  `document_id` bigint NOT NULL,
  `business_key` varchar(160) NOT NULL,
  `document_no` varchar(100) NOT NULL,
  `amount` decimal(18,2) NOT NULL,
  `account_set_id` bigint NOT NULL,
  `posting_user_id` bigint NOT NULL,
  `voucher_id` bigint DEFAULT NULL,
  `reversal_voucher_id` bigint DEFAULT NULL,
  `state` varchar(16) NOT NULL,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `active_document_id` bigint GENERATED ALWAYS AS (CASE WHEN state='ACTIVE' THEN document_id ELSE NULL END) STORED,
  UNIQUE KEY uk_active_document (tenant_id,source,active_document_id),
  UNIQUE KEY uk_business_key (tenant_id,source,business_key),
  KEY idx_document (tenant_id,source,document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `integration_stock_intent` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `source` varchar(16) NOT NULL,
  `business_key` varchar(160) NOT NULL,
  `payload_hash` varchar(64) NOT NULL,
  `state` varchar(16) NOT NULL DEFAULT 'WAITING',
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_intent (tenant_id,source,business_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `wms_integration_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `kind` varchar(16) NOT NULL,
  `source` varchar(16) NOT NULL,
  `source_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `enabled` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_mapping (tenant_id,kind,source,source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `wms_integration_movement` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `source` varchar(16) NOT NULL,
  `business_key` varchar(160) NOT NULL,
  `payload_hash` varchar(64) DEFAULT NULL,
  `state` varchar(16) NOT NULL,
  `sku_id` bigint DEFAULT NULL,
  `warehouse_id` bigint DEFAULT NULL,
  `quantity` decimal(24,8) DEFAULT NULL,
  `price` decimal(24,8) DEFAULT NULL,
  `document_id` bigint DEFAULT NULL,
  `document_no` varchar(200) DEFAULT NULL,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_movement (tenant_id,source,business_key),
  KEY idx_reconcile (state,create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `integration_inbox` (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `tenant_id` bigint NOT NULL,
  `consumer` varchar(100) NOT NULL,
  `business_key` varchar(200) NOT NULL,
  `payload_hash` varchar(64) NOT NULL,
  `status` varchar(16) NOT NULL,
  `result_json` longtext DEFAULT NULL,
  `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_target (tenant_id,consumer,business_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
