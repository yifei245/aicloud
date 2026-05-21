USE aicloud;

INSERT INTO ai_tenant(id, name, code, status) VALUES (1, '默认租户', 'default', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

INSERT INTO ai_role(id, tenant_id, code, name, data_scope, sort, status) VALUES
(1, 1, 'SUPER_ADMIN', '超级管理员', 'ALL', 1, 1),
(2, 1, 'ADMIN', '系统管理员', 'ALL', 2, 1),
(3, 1, 'APP_USER', '终端用户', 'SELF', 3, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

INSERT INTO ai_menu(id, tenant_id, parent_id, name, type, path, component, permission, icon, sort, status) VALUES
(1, 1, 0, '系统管理', 1, '/system', NULL, NULL, 'setting', 1, 1),
(2, 1, 1, '用户管理', 2, '/system/user', 'system/user/index', 'system:user:list', 'user', 1, 1),
(3, 1, 2, '用户查询', 3, NULL, NULL, 'system:user:query', NULL, 1, 1),
(4, 1, 2, '用户新增', 3, NULL, NULL, 'system:user:create', NULL, 2, 1),
(5, 1, 2, '用户修改', 3, NULL, NULL, 'system:user:update', NULL, 3, 1),
(9, 1, 2, '用户重置密码', 3, NULL, NULL, 'system:user:reset-password', NULL, 4, 1),
(10, 1, 2, '用户删除', 3, NULL, NULL, 'system:user:delete', NULL, 5, 1),
(6, 1, 1, '角色管理', 2, '/system/role', 'system/role/index', 'system:role:list', 'peoples', 2, 1),
(7, 1, 6, '角色授权', 3, NULL, NULL, 'system:role:assign', NULL, 1, 1),
(11, 1, 6, '角色查询', 3, NULL, NULL, 'system:role:query', NULL, 2, 1),
(12, 1, 6, '角色新增', 3, NULL, NULL, 'system:role:create', NULL, 3, 1),
(13, 1, 6, '角色修改', 3, NULL, NULL, 'system:role:update', NULL, 4, 1),
(14, 1, 6, '角色删除', 3, NULL, NULL, 'system:role:delete', NULL, 5, 1),
(8, 1, 1, '菜单管理', 2, '/system/menu', 'system/menu/index', 'system:menu:list', 'tree', 3, 1),
(15, 1, 1, '部门管理', 2, '/system/dept', 'system/dept/index', 'system:dept:list', 'apartment', 4, 1),
(16, 1, 15, '部门查询', 3, NULL, NULL, 'system:dept:query', NULL, 1, 1),
(17, 1, 15, '部门新增', 3, NULL, NULL, 'system:dept:create', NULL, 2, 1),
(18, 1, 15, '部门修改', 3, NULL, NULL, 'system:dept:update', NULL, 3, 1),
(19, 1, 15, '部门删除', 3, NULL, NULL, 'system:dept:delete', NULL, 4, 1),
(20, 1, 1, '岗位管理', 2, '/system/post', 'system/post/index', 'system:post:list', 'post', 5, 1),
(21, 1, 20, '岗位查询', 3, NULL, NULL, 'system:post:query', NULL, 1, 1),
(22, 1, 20, '岗位新增', 3, NULL, NULL, 'system:post:create', NULL, 2, 1),
(23, 1, 20, '岗位修改', 3, NULL, NULL, 'system:post:update', NULL, 3, 1),
(24, 1, 20, '岗位删除', 3, NULL, NULL, 'system:post:delete', NULL, 4, 1),
(25, 1, 1, '字典管理', 2, '/system/dict', 'system/dict/index', 'system:dict:list', 'dict', 6, 1),
(26, 1, 25, '字典查询', 3, NULL, NULL, 'system:dict:query', NULL, 1, 1),
(27, 1, 25, '字典新增', 3, NULL, NULL, 'system:dict:create', NULL, 2, 1),
(28, 1, 25, '字典修改', 3, NULL, NULL, 'system:dict:update', NULL, 3, 1),
(29, 1, 25, '字典删除', 3, NULL, NULL, 'system:dict:delete', NULL, 4, 1),
(30, 1, 1, '参数配置', 2, '/system/config', 'system/config/index', 'system:config:list', 'config', 7, 1),
(31, 1, 30, '参数查询', 3, NULL, NULL, 'system:config:query', NULL, 1, 1),
(32, 1, 30, '参数修改', 3, NULL, NULL, 'system:config:update', NULL, 2, 1),
(33, 1, 30, '参数删除', 3, NULL, NULL, 'system:config:delete', NULL, 3, 1),
(34, 1, 1, '日志管理', 2, '/system/log', 'system/log/index', 'system:log:list', 'log', 8, 1),
(35, 1, 34, '日志查询', 3, NULL, NULL, 'system:log:query', NULL, 1, 1),
(36, 1, 8, '菜单查询', 3, NULL, NULL, 'system:menu:list', NULL, 1, 1)
,
(100, 1, 0, '业务中心', 1, '/biz', NULL, NULL, 'shop', 2, 1),
(101, 1, 100, '会员中心', 2, '/member/profile', 'member/profile/index', 'member:profile:list', 'member', 1, 1),
(102, 1, 101, '会员查询', 3, NULL, NULL, 'member:profile:query', NULL, 1, 1),
(103, 1, 100, '商品中心', 2, '/product/spu', 'product/spu/index', 'product:spu:list', 'goods', 2, 1),
(104, 1, 103, '商品查询', 3, NULL, NULL, 'product:spu:query', NULL, 1, 1),
(105, 1, 103, '商品保存', 3, NULL, NULL, 'product:spu:save', NULL, 2, 1),
(106, 1, 100, '营销中心', 2, '/promotion/coupon', 'promotion/coupon/index', 'promotion:coupon:list', 'coupon', 3, 1),
(107, 1, 106, '优惠券查询', 3, NULL, NULL, 'promotion:coupon:query', NULL, 1, 1),
(108, 1, 106, '优惠券保存', 3, NULL, NULL, 'promotion:coupon:save', NULL, 2, 1),
(109, 1, 100, '交易中心', 2, '/trade/order', 'trade/order/index', 'trade:order:list', 'order', 4, 1),
(110, 1, 109, '订单查询', 3, NULL, NULL, 'trade:order:query', NULL, 1, 1),
(111, 1, 109, '订单状态变更', 3, NULL, NULL, 'trade:order:update-status', NULL, 2, 1),
(112, 1, 100, '支付中心', 2, '/pay/order', 'pay/order/index', 'pay:order:list', 'pay', 5, 1),
(113, 1, 112, '支付单查询', 3, NULL, NULL, 'pay:order:query', NULL, 1, 1),
(114, 1, 112, '支付单状态变更', 3, NULL, NULL, 'pay:order:update-status', NULL, 2, 1),
(115, 1, 100, '商户中心', 2, '/merchant/profile', 'merchant/profile/index', 'merchant:profile:list', 'merchant', 6, 1),
(116, 1, 115, '商户查询', 3, NULL, NULL, 'merchant:profile:query', NULL, 1, 1),
(117, 1, 115, '商户保存', 3, NULL, NULL, 'merchant:profile:save', NULL, 2, 1),
(118, 1, 100, 'CRM', 2, '/crm/customer', 'crm/customer/index', 'crm:customer:list', 'crm', 7, 1),
(119, 1, 118, '客户查询', 3, NULL, NULL, 'crm:customer:query', NULL, 1, 1),
(120, 1, 118, '客户保存', 3, NULL, NULL, 'crm:customer:save', NULL, 2, 1),
(121, 1, 118, '跟进记录', 3, NULL, NULL, 'crm:follow:create', NULL, 3, 1),
(122, 1, 118, '商机保存', 3, NULL, NULL, 'crm:opportunity:save', NULL, 4, 1),
(123, 1, 100, 'ERP', 2, '/erp/inventory', 'erp/inventory/index', 'erp:inventory:list', 'inventory', 8, 1),
(124, 1, 123, '库存查询', 3, NULL, NULL, 'erp:inventory:query', NULL, 1, 1),
(125, 1, 123, '库存调整', 3, NULL, NULL, 'erp:inventory:adjust', NULL, 2, 1),
(126, 1, 123, '盘点单保存', 3, NULL, NULL, 'erp:stock-check:save', NULL, 3, 1),
(127, 1, 100, '工作流', 2, '/bpm/process', 'bpm/process/index', 'bpm:process:list', 'flow', 9, 1),
(128, 1, 127, '流程定义保存', 3, NULL, NULL, 'bpm:process-definition:save', NULL, 1, 1),
(129, 1, 127, '流程发起', 3, NULL, NULL, 'bpm:process:start', NULL, 2, 1),
(130, 1, 127, '任务处理', 3, NULL, NULL, 'bpm:task:complete', NULL, 3, 1),
(131, 1, 100, '报表中心', 2, '/report/dashboard', 'report/dashboard/index', 'report:dashboard:view', 'chart', 10, 1),
(132, 1, 131, '销售报表', 3, NULL, NULL, 'report:sales:overview', NULL, 1, 1),
(133, 1, 131, '经营看板', 3, NULL, NULL, 'report:dashboard:operation', NULL, 2, 1),
(134, 1, 131, 'CRM 报表', 3, NULL, NULL, 'report:crm:overview', NULL, 3, 1),
(135, 1, 100, '开放平台', 2, '/openapi/app', 'openapi/app/index', 'openapi:app:list', 'api', 11, 1),
(136, 1, 135, '开放平台查询', 3, NULL, NULL, 'openapi:app:query', NULL, 1, 1)

ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

INSERT INTO ai_dept(id, tenant_id, parent_id, name, leader_user_id, sort, status) VALUES
(1, 1, 0, '总部', 1, 1, 1),
(2, 1, 1, '技术部', 1, 2, 1),
(3, 1, 1, '运营部', 2, 3, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

INSERT INTO ai_post(id, tenant_id, code, name, sort, status) VALUES
(1, 1, 'CEO', '总经理', 1, 1),
(2, 1, 'DEV_LEADER', '技术负责人', 2, 1),
(3, 1, 'OPS_LEADER', '运营负责人', 3, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

INSERT INTO ai_dict_type(id, tenant_id, dict_type, dict_name, status) VALUES
(1, 1, 'user_type', '用户类型', 1),
(2, 1, 'menu_type', '菜单类型', 1),
(3, 1, 'common_status', '通用状态', 1)
ON DUPLICATE KEY UPDATE dict_name=VALUES(dict_name), status=VALUES(status);

INSERT INTO ai_dict_data(id, tenant_id, dict_type, dict_label, dict_value, sort, status) VALUES
(1, 1, 'user_type', '管理用户', 'ADMIN', 1, 1),
(2, 1, 'user_type', '终端用户', 'MEMBER', 2, 1),
(3, 1, 'menu_type', '目录', '1', 1, 1),
(4, 1, 'menu_type', '菜单', '2', 2, 1),
(5, 1, 'menu_type', '按钮', '3', 3, 1),
(6, 1, 'common_status', '启用', '1', 1, 1),
(7, 1, 'common_status', '停用', '0', 2, 1)
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label), status=VALUES(status);

INSERT INTO ai_system_config(id, tenant_id, config_key, config_name, config_value, status, remark) VALUES
(1, 1, 'sys.user.init-password', '用户初始密码', '123456', 1, '系统创建用户时的默认密码'),
(2, 1, 'sys.login.captcha-enabled', '登录验证码开关', 'false', 1, '演示环境默认关闭'),
(3, 1, 'sys.notice.auto-publish', '公告自动发布开关', 'true', 1, '为后续公告模块预留')
ON DUPLICATE KEY UPDATE config_name=VALUES(config_name), config_value=VALUES(config_value), status=VALUES(status), remark=VALUES(remark);
