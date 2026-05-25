USE aicloud;
SET NAMES utf8mb4;

INSERT INTO ai_user(id, tenant_id, username, password, nickname, mobile, email, user_type, status)
VALUES
(1, 1, 'admin', '123456', '超级管理员', '13800000000', 'admin@aicloud.com', 'ADMIN', 1),
(2, 1, 'ops', '123456', '运维管理员', '13800000001', 'ops@aicloud.com', 'ADMIN', 1),
(3, 1, 'appuser', '123456', 'APP用户', '13800000002', 'app@aicloud.com', 'MEMBER', 1)
ON DUPLICATE KEY UPDATE nickname=VALUES(nickname), status=VALUES(status);

INSERT INTO ai_user_role(id, tenant_id, user_id, role_id) VALUES
(1, 1, 1, 1),
(2, 1, 2, 2),
(3, 1, 3, 3)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

INSERT INTO ai_user_dept_post(id, tenant_id, user_id, dept_id, post_id) VALUES
(1, 1, 1, 1, 1),
(2, 1, 2, 3, 3),
(3, 1, 3, 2, 2)
ON DUPLICATE KEY UPDATE dept_id=VALUES(dept_id), post_id=VALUES(post_id);

INSERT INTO ai_role_menu(id, tenant_id, role_id, menu_id) VALUES
(1, 1, 1, 1),(2, 1, 1, 2),(3, 1, 1, 3),(4, 1, 1, 4),(5, 1, 1, 5),(6, 1, 1, 6),(7, 1, 1, 7),(8, 1, 1, 8),
(9, 1, 2, 1),(10, 1, 2, 2),(11, 1, 2, 3),(12, 1, 2, 6),(13, 1, 2, 8),
(14, 1, 1, 9),(15, 1, 1, 10),(16, 1, 1, 11),(17, 1, 1, 12),(18, 1, 1, 13),(19, 1, 1, 14),
(20, 1, 1, 15),(21, 1, 1, 16),(22, 1, 1, 17),(23, 1, 1, 18),(24, 1, 1, 19),
(25, 1, 1, 20),(26, 1, 1, 21),(27, 1, 1, 22),(28, 1, 1, 23),(29, 1, 1, 24),
(30, 1, 1, 25),(31, 1, 1, 26),(32, 1, 1, 27),(33, 1, 1, 28),(34, 1, 1, 29),
(35, 1, 1, 30),(36, 1, 1, 31),(37, 1, 1, 32),(38, 1, 1, 33),(39, 1, 1, 34),(40, 1, 1, 35),
(41, 1, 2, 9),(42, 1, 2, 10),(43, 1, 2, 11),(44, 1, 2, 12),(45, 1, 2, 13),(46, 1, 2, 14),
(47, 1, 2, 15),(48, 1, 2, 16),(49, 1, 2, 17),(50, 1, 2, 18),(51, 1, 2, 19),
(52, 1, 2, 20),(53, 1, 2, 21),(54, 1, 2, 22),(55, 1, 2, 23),(56, 1, 2, 24),
(57, 1, 2, 25),(58, 1, 2, 26),(59, 1, 2, 27),(60, 1, 2, 28),(61, 1, 2, 29),
(62, 1, 2, 30),(63, 1, 2, 31),(64, 1, 2, 32),(65, 1, 2, 33),(66, 1, 2, 34),(67, 1, 2, 35),
(68, 1, 1, 36),
(69, 1, 1, 100),(70, 1, 1, 101),(71, 1, 1, 102),(72, 1, 1, 103),(73, 1, 1, 104),(74, 1, 1, 105),
(75, 1, 1, 106),(76, 1, 1, 107),(77, 1, 1, 108),(78, 1, 1, 109),(79, 1, 1, 110),(80, 1, 1, 111),
(81, 1, 1, 112),(82, 1, 1, 113),(83, 1, 1, 114),(84, 1, 1, 115),(85, 1, 1, 116),(86, 1, 1, 117),
(87, 1, 1, 118),(88, 1, 1, 119),(89, 1, 1, 120),(90, 1, 1, 121),(91, 1, 1, 122),(92, 1, 1, 123),
(93, 1, 1, 124),(94, 1, 1, 125),(95, 1, 1, 126),(96, 1, 1, 127),(97, 1, 1, 128),(98, 1, 1, 129),
(99, 1, 1, 130),(100, 1, 1, 131),(101, 1, 1, 132),(102, 1, 1, 133),(103, 1, 1, 134),(104, 1, 1, 135),(105, 1, 1, 136),
(106, 1, 2, 36),
(107, 1, 2, 100),(108, 1, 2, 101),(109, 1, 2, 102),(110, 1, 2, 103),(111, 1, 2, 104),(112, 1, 2, 105),
(113, 1, 2, 106),(114, 1, 2, 107),(115, 1, 2, 108),(116, 1, 2, 109),(117, 1, 2, 110),(118, 1, 2, 111),
(119, 1, 2, 112),(120, 1, 2, 113),(121, 1, 2, 114),(122, 1, 2, 115),(123, 1, 2, 116),(124, 1, 2, 117),
(125, 1, 2, 118),(126, 1, 2, 119),(127, 1, 2, 120),(128, 1, 2, 121),(129, 1, 2, 122),(130, 1, 2, 123),
(131, 1, 2, 124),(132, 1, 2, 125),(133, 1, 2, 126),(134, 1, 2, 127),(135, 1, 2, 128),(136, 1, 2, 129),
(137, 1, 2, 130),(138, 1, 2, 131),(139, 1, 2, 132),(140, 1, 2, 133),(141, 1, 2, 134),(142, 1, 2, 135),(143, 1, 2, 136)
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);

INSERT INTO ai_login_log(id, tenant_id, user_id, username, user_type, login_terminal, login_ip, success, error_msg) VALUES
(1, 1, 1, 'admin', 'ADMIN', 'ADMIN', '127.0.0.1', 1, NULL),
(2, 1, 2, 'ops', 'ADMIN', 'ADMIN', '127.0.0.1', 1, NULL),
(3, 1, NULL, 'bad-user', NULL, 'ADMIN', '127.0.0.1', 0, '用户名或密码错误')
ON DUPLICATE KEY UPDATE success=VALUES(success), error_msg=VALUES(error_msg);

INSERT INTO ai_oauth2_client(id, tenant_id, client_id, client_secret, grant_types, scopes, redirect_uris, access_token_validity, refresh_token_validity, status) VALUES
(1, 1, 'aicloud-admin-web', 'admin-secret', 'authorization_code,password,refresh_token,client_credentials', 'admin,system,openid', 'http://localhost:3000/callback', 7200, 2592000, 1),
(2, 1, 'aicloud-app', 'app-secret', 'password,refresh_token,client_credentials', 'app,member,openid', NULL, 7200, 2592000, 1),
(3, 1, 'aicloud-openapi', 'openapi-secret', 'client_credentials', 'openapi', NULL, 3600, 2592000, 1)
ON DUPLICATE KEY UPDATE client_secret=VALUES(client_secret), status=VALUES(status);

INSERT INTO ai_terminal_client(id, tenant_id, terminal_code, client_id, status) VALUES
(1, 1, 'ADMIN', 'aicloud-admin-web', 1),
(2, 1, 'WEB', 'aicloud-admin-web', 1),
(3, 1, 'APP', 'aicloud-app', 1),
(4, 1, 'MP', 'aicloud-app', 1),
(5, 1, 'OPENAPI', 'aicloud-openapi', 1)
ON DUPLICATE KEY UPDATE client_id=VALUES(client_id), status=VALUES(status);

INSERT INTO ai_api_app(id, tenant_id, app_key, app_secret, app_name, sign_type, ip_whitelist, status) VALUES
(1, 1, 'demo_app_key', 'demo_app_secret', '默认第三方应用', 'HMAC_SHA256', NULL, 1)
ON DUPLICATE KEY UPDATE app_secret=VALUES(app_secret), status=VALUES(status);


SET @aicloud_add_refresh_expire_time := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE ai_sso_session ADD COLUMN refresh_expire_time DATETIME NULL AFTER expire_time',
    'SELECT 1')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'aicloud' AND TABLE_NAME = 'ai_sso_session' AND COLUMN_NAME = 'refresh_expire_time'
);
PREPARE aicloud_stmt FROM @aicloud_add_refresh_expire_time;
EXECUTE aicloud_stmt;
DEALLOCATE PREPARE aicloud_stmt;
UPDATE ai_sso_session SET refresh_expire_time = DATE_ADD(expire_time, INTERVAL 30 DAY) WHERE refresh_expire_time IS NULL;
USE aicloud;
INSERT INTO ai_tenant(id, name, code, status) VALUES (2, '演示租户2', 'tenant2', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), code=VALUES(code), status=VALUES(status);

INSERT INTO ai_role(id, tenant_id, code, name, data_scope, sort, status)
SELECT id + 2000, 2, code, CONCAT(name, '-租户2'), data_scope, sort, status FROM ai_role WHERE tenant_id = 1
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

INSERT INTO ai_menu(id, tenant_id, parent_id, name, type, path, component, permission, icon, visible, sort, status)
SELECT id + 2000, 2, IF(parent_id = 0, 0, parent_id + 2000), name, type, path, component, permission, icon, visible, sort, status
FROM ai_menu WHERE tenant_id = 1
ON DUPLICATE KEY UPDATE name=VALUES(name), path=VALUES(path), component=VALUES(component), permission=VALUES(permission), icon=VALUES(icon), visible=VALUES(visible), sort=VALUES(sort), status=VALUES(status);

INSERT INTO ai_dept(id, tenant_id, parent_id, name, leader_user_id, sort, status)
SELECT id + 2000, 2, IF(parent_id = 0, 0, parent_id + 2000), CONCAT(name, '-T2'), NULL, sort, status FROM ai_dept WHERE tenant_id = 1
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

INSERT INTO ai_post(id, tenant_id, code, name, sort, status)
SELECT id + 2000, 2, code, CONCAT(name, '-T2'), sort, status FROM ai_post WHERE tenant_id = 1
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

INSERT INTO ai_user(id, tenant_id, username, password, nickname, mobile, email, user_type, status)
VALUES
(2001, 2, 'admin', '123456', '租户2超级管理员', '13900002001', 'admin-t2@aicloud.com', 'ADMIN', 1),
(2002, 2, 'ops', '123456', '租户2运维管理员', '13900002002', 'ops-t2@aicloud.com', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE nickname=VALUES(nickname), mobile=VALUES(mobile), email=VALUES(email), status=VALUES(status);

INSERT INTO ai_user_role(id, tenant_id, user_id, role_id) VALUES
(2001, 2, 2001, 2001),
(2002, 2, 2002, 2002)
ON DUPLICATE KEY UPDATE role_id=VALUES(role_id);

INSERT INTO ai_user_dept_post(id, tenant_id, user_id, dept_id, post_id) VALUES
(2001, 2, 2001, 2001, 2001),
(2002, 2, 2002, 2003, 2003)
ON DUPLICATE KEY UPDATE dept_id=VALUES(dept_id), post_id=VALUES(post_id);

INSERT INTO ai_role_menu(tenant_id, role_id, menu_id)
SELECT 2, rm.role_id + 2000, rm.menu_id + 2000 FROM ai_role_menu rm WHERE rm.tenant_id = 1
ON DUPLICATE KEY UPDATE menu_id=VALUES(menu_id);

INSERT INTO ai_oauth2_client(id, tenant_id, client_id, client_secret, grant_types, scopes, redirect_uris, access_token_validity, refresh_token_validity, status)
SELECT id + 2000, 2, client_id, client_secret, grant_types, scopes, redirect_uris, access_token_validity, refresh_token_validity, status FROM ai_oauth2_client WHERE tenant_id = 1
ON DUPLICATE KEY UPDATE client_secret=VALUES(client_secret), status=VALUES(status);

INSERT INTO ai_terminal_client(id, tenant_id, terminal_code, client_id, status)
SELECT id + 2000, 2, terminal_code, client_id, status FROM ai_terminal_client WHERE tenant_id = 1
ON DUPLICATE KEY UPDATE client_id=VALUES(client_id), status=VALUES(status);
