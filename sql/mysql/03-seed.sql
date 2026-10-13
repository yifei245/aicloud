USE aicloud;
-- 演示管理账号关联员工档案，启用员工自助服务；已有员工数据不会被覆盖。
INSERT INTO `hrm_employee` (`id`,`name`,`job_number`,`user_id`,`mobile`,`dept_id`,`entry_status`,`status`,`type`,`entry_time`,`probation`,`tenant_id`)
SELECT 1,'演示管理员','AICLOUD001',1,'13800000001',1,1,2,1,'2026-01-01 09:00:00',0,1
WHERE NOT EXISTS (SELECT 1 FROM `hrm_employee` WHERE `user_id`=1 AND `tenant_id`=1 AND `deleted`=0)
ON DUPLICATE KEY UPDATE `id`=`hrm_employee`.`id`;
-- Development-only accounts all use Aicloud@123; replace credentials before exposing services.
INSERT INTO system_oauth2_client (id,client_id,secret,name,logo,status,access_token_validity_seconds,refresh_token_validity_seconds,redirect_uris,authorized_grant_types,scopes,auto_approve_scopes) VALUES
(5,'sso-public','','SSO Public PKCE','',0,1800,604800,'["http://localhost:3003/callback"]','["authorization_code","refresh_token"]','["user.read"]','[]'),
(10,'member-web','local-server-only','Web 用户端','',0,1800,604800,'[]','["refresh_token"]','[]','[]'),
(11,'member-app','local-server-only','手机 App','',0,1800,604800,'[]','["refresh_token"]','[]','[]'),
(12,'member-miniapp','local-server-only','微信小程序','',0,1800,604800,'[]','["refresh_token"]','[]','[]'),
(13,'member-h5','local-server-only','H5','',0,1800,604800,'[]','["refresh_token"]','[]','[]'),
(14,'member-wechat','local-server-only','微信公众号','',0,1800,604800,'[]','["refresh_token"]','[]','[]') ON DUPLICATE KEY UPDATE id=system_oauth2_client.id;
INSERT INTO system_tenant (id,name,contact_user_id,contact_name,contact_mobile,status,websites,package_id,expire_time,account_count) VALUES
(1,'aicloud',1,'管理员','',0,'["localhost","127.0.0.1"]',0,'2099-12-31 23:59:59',99999) ON DUPLICATE KEY UPDATE id=system_tenant.id;
INSERT INTO system_dept (id,name,parent_id,sort,status,tenant_id) VALUES (1,'aicloud',0,0,0,1) ON DUPLICATE KEY UPDATE id=system_dept.id;
INSERT INTO system_users (id,username,password,nickname,dept_id,status,tenant_id) VALUES
(1,'admin','$2a$10$iMVa077eeynPXIri1C1tOeWbPOc1GXY7YFuLEdFeR3l09CskRMZrW','aicloud 管理员',1,0,1),
(2,'viewer','$2a$10$iMVa077eeynPXIri1C1tOeWbPOc1GXY7YFuLEdFeR3l09CskRMZrW','只读演示用户',1,0,1) ON DUPLICATE KEY UPDATE id=system_users.id;
INSERT INTO system_role (id,name,code,sort,status,type,data_scope,data_scope_dept_ids,tenant_id) VALUES
(1,'超级管理员','super_admin',0,0,1,1,'[]',1),
(2,'只读用户','viewer',1,0,2,5,'[]',1) ON DUPLICATE KEY UPDATE id=system_role.id;
INSERT INTO system_user_role (user_id,role_id,tenant_id) VALUES (1,1,1),(2,2,1) ON DUPLICATE KEY UPDATE id=system_user_role.id;
INSERT INTO system_role_menu (role_id,menu_id,tenant_id) SELECT 1,id,1 FROM system_menu WHERE deleted=0 ON DUPLICATE KEY UPDATE id=system_role_menu.id;
INSERT INTO system_role_menu (role_id,menu_id,tenant_id) SELECT 2,id,1 FROM system_menu WHERE permission='system:user:query' AND deleted=0 ON DUPLICATE KEY UPDATE id=system_role_menu.id;
INSERT INTO system_oauth2_client (id,client_id,secret,name,logo,status,access_token_validity_seconds,refresh_token_validity_seconds,redirect_uris,authorized_grant_types,scopes,auto_approve_scopes) VALUES
(1,'default','aicloud_dev_default_secret','管理端','',0,1800,604800,'["http://localhost:3000/callback"]','["password","refresh_token","authorization_code"]','["user.read","user.write"]','[]'),
(2,'sso-web','aicloud_dev_sso_secret','SSO Web','',0,1800,604800,'["http://localhost:3001/callback"]','["authorization_code","refresh_token"]','["user.read"]','[]'),
(3,'sso-mobile','aicloud_dev_mobile_secret','SSO Mobile BFF','',0,1800,604800,'["http://localhost:3002/callback"]','["authorization_code","refresh_token"]','["user.read"]','[]'),
(4,'third-party','aicloud_dev_api_secret','第三方应用','',0,900,3600,'[]','["client_credentials"]','["dictionary.read"]','[]') ON DUPLICATE KEY UPDATE id=system_oauth2_client.id;
INSERT INTO infra_file_config (id,name,storage,master,config) VALUES
(1,'本地文件',10,1,'{"@class":"com.aicloud.module.infra.framework.file.core.client.local.LocalFileClientConfig","basePath":"./data/files","domain":"http://localhost:18080"}') ON DUPLICATE KEY UPDATE id=infra_file_config.id;
INSERT INTO member_user (id,mobile,password,nickname,status,register_ip,tenant_id) VALUES
(1,'13800000000','$2a$10$iMVa077eeynPXIri1C1tOeWbPOc1GXY7YFuLEdFeR3l09CskRMZrW','演示会员',0,'127.0.0.1',1) ON DUPLICATE KEY UPDATE id=member_user.id;

-- Repair only the original aicloud local-storage seed; keep user-defined domains.
UPDATE infra_file_config SET config=JSON_SET(config, '$.domain', 'http://localhost:18080') WHERE id=1 AND storage=10 AND JSON_UNQUOTE(JSON_EXTRACT(config, '$.domain'))='http://localhost:18080/admin-api/infra/file';
