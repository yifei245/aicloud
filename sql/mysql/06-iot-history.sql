-- aicloud IoT message and property history; MySQL only, MIT.
USE aicloud;
CREATE TABLE IF NOT EXISTS `iot_device_message` (
  `id` varchar(64) NOT NULL,
  `tenant_id` bigint NOT NULL,
  `device_id` bigint NOT NULL,
  `ts` bigint NOT NULL,
  `report_time` bigint DEFAULT NULL,
  `server_id` varchar(64) DEFAULT NULL,
  `upstream` boolean NOT NULL,
  `reply` boolean NOT NULL,
  `identifier` varchar(255) DEFAULT NULL,
  `request_id` varchar(64) DEFAULT NULL,
  `method` varchar(100) NOT NULL,
  `params` json DEFAULT NULL,
  `data` json DEFAULT NULL,
  `code` int DEFAULT NULL,
  `msg` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_device_time` (`tenant_id`,`device_id`,`ts`),
  KEY `idx_tenant_time` (`tenant_id`,`ts`),
  KEY `idx_device_request_reply` (`tenant_id`,`device_id`,`request_id`,`reply`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `iot_device_property_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `device_id` bigint NOT NULL,
  `identifier` varchar(255) NOT NULL,
  `value` json NOT NULL,
  `ts` bigint NOT NULL,
  `report_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_device_property_time` (`tenant_id`,`device_id`,`identifier`,`ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
