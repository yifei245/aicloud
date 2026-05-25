USE aicloud;
SET NAMES utf8mb4;

INSERT INTO ai_member_level(id, tenant_id, level_code, level_name, threshold_points, discount_rate, status)
VALUES
(1, 1, 'NORMAL', '普通会员', 0, 100.00, 1),
(2, 1, 'VIP', 'VIP会员', 1000, 95.00, 1),
(3, 1, 'SVIP', 'SVIP会员', 5000, 90.00, 1)
ON DUPLICATE KEY UPDATE level_name=VALUES(level_name), threshold_points=VALUES(threshold_points), discount_rate=VALUES(discount_rate), status=VALUES(status);

INSERT INTO ai_member_profile(tenant_id, user_id, nickname, mobile, email, avatar, gender, birthday, level, points, balance, status)
VALUES (1, 3, 'APP用户', '13800000002', 'app@aicloud.com', 'https://static.aicloud.local/avatar/appuser.png', 1, '1998-08-08', 'VIP', 1000, 99.00, 1)
ON DUPLICATE KEY UPDATE nickname=VALUES(nickname), mobile=VALUES(mobile), email=VALUES(email), avatar=VALUES(avatar), gender=VALUES(gender), birthday=VALUES(birthday), level=VALUES(level), points=VALUES(points), balance=VALUES(balance), status=VALUES(status);

INSERT INTO ai_member_address(id, tenant_id, user_id, receiver_name, mobile, province, city, district, detail_address, default_status, status)
VALUES
(1, 1, 3, 'APP用户', '13800000002', '广东省', '深圳市', '南山区', '科技园科苑路 88 号', 1, 1),
(2, 1, 3, 'APP用户-公司', '13800000002', '广东省', '深圳市', '福田区', '车公庙泰然九路 1 号', 0, 1)
ON DUPLICATE KEY UPDATE receiver_name=VALUES(receiver_name), mobile=VALUES(mobile), province=VALUES(province), city=VALUES(city), district=VALUES(district), detail_address=VALUES(detail_address), default_status=VALUES(default_status), status=VALUES(status);

INSERT INTO ai_member_account_log(id, tenant_id, user_id, biz_type, change_type, points_delta, balance_delta, points_after, balance_after, remark)
VALUES
(1, 1, 3, 'REGISTER', 'INCR', 100, 0.00, 100, 0.00, '注册赠送积分'),
(2, 1, 3, 'PAY', 'INCR', 900, 99.00, 1000, 99.00, '首单支付赠送积分与余额'),
(3, 1, 3, 'OPENAPI_SYNC', 'DECR', 0, -10.00, 1000, 89.00, '开放平台扣减余额示例')
ON DUPLICATE KEY UPDATE points_delta=VALUES(points_delta), balance_delta=VALUES(balance_delta), points_after=VALUES(points_after), balance_after=VALUES(balance_after), remark=VALUES(remark);

INSERT INTO ai_mp_user_bind(id, tenant_id, user_id, open_id, union_id, nickname, avatar_url, phone, status)
VALUES
(1, 1, 3, 'mp_openid_demo_001', 'mp_union_demo_001', '小程序APP用户', 'https://static.aicloud.local/avatar/mp-appuser.png', '13800000002', 1)
ON DUPLICATE KEY UPDATE union_id=VALUES(union_id), nickname=VALUES(nickname), avatar_url=VALUES(avatar_url), phone=VALUES(phone), status=VALUES(status);

INSERT INTO ai_product_category(id, tenant_id, parent_id, name, sort, status)
VALUES
(1000, 1, 0, '云产品', 10, 1),
(1001, 1, 1000, '云主机', 20, 1),
(1002, 1, 1000, '云数据库', 30, 1)
ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id), name=VALUES(name), sort=VALUES(sort), status=VALUES(status);

INSERT INTO ai_product_spu(id, tenant_id, spu_no, name, sub_title, category_id, brand_name, unit_name, cover_url, sort, status, sale_price, stock)
VALUES
(1, 1, 'SPU-10001', 'AI 云主机基础版', '适合个人与轻量业务部署', 1001, 'AICloud', '台', 'https://static.aicloud.local/product/host-basic.png', 100, 1, 99.00, 1000),
(2, 1, 'SPU-10002', 'AI 云数据库套餐', '高可用 MySQL 云数据库', 1002, 'AICloud', '套', 'https://static.aicloud.local/product/db-mysql.png', 90, 1, 199.00, 500)
ON DUPLICATE KEY UPDATE spu_no=VALUES(spu_no), sub_title=VALUES(sub_title), category_id=VALUES(category_id), brand_name=VALUES(brand_name), unit_name=VALUES(unit_name), cover_url=VALUES(cover_url), sort=VALUES(sort), sale_price=VALUES(sale_price), stock=VALUES(stock), status=VALUES(status);

INSERT INTO ai_promotion_coupon_template(id, tenant_id, template_no, name, discount_type, discount_value, min_amount, total_count, claim_count, receive_limit, start_time, end_time, status, remark)
VALUES
(1, 1, 'TPL-10001', '新客满100减10券', 'CASH', 10.00, 100.00, 10000, 1, 1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, '新客首单可领取'),
(2, 1, 'TPL-10002', '会员95折券', 'DISCOUNT', 95.00, 50.00, 5000, 1, 2, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, '会员通用折扣券')
ON DUPLICATE KEY UPDATE name=VALUES(name), discount_type=VALUES(discount_type), discount_value=VALUES(discount_value), min_amount=VALUES(min_amount), total_count=VALUES(total_count), claim_count=VALUES(claim_count), receive_limit=VALUES(receive_limit), start_time=VALUES(start_time), end_time=VALUES(end_time), status=VALUES(status), remark=VALUES(remark);

INSERT INTO ai_promotion_user_coupon(id, tenant_id, template_id, user_id, coupon_code, status, claim_time, expire_time)
VALUES
(1, 1, 1, 3, 'CPN-10001', 'UNUSED', '2026-05-01 10:00:00', '2026-12-31 23:59:59'),
(2, 1, 2, 3, 'CPN-10002', 'UNUSED', '2026-05-02 10:00:00', '2026-12-31 23:59:59')
ON DUPLICATE KEY UPDATE template_id=VALUES(template_id), status=VALUES(status), claim_time=VALUES(claim_time), expire_time=VALUES(expire_time);


INSERT INTO ai_merchant_profile(id, tenant_id, merchant_no, merchant_name, contact_name, contact_mobile, contact_email, settle_account, status, audit_status)
VALUES
(1, 1, 'M-10001', 'AICloud 深圳运营中心', '张经理', '13800001001', 'merchant@aicloud.com', '招商银行 6222000000000001', 'ENABLED', 'APPROVED')
ON DUPLICATE KEY UPDATE merchant_name=VALUES(merchant_name), contact_name=VALUES(contact_name), contact_mobile=VALUES(contact_mobile), contact_email=VALUES(contact_email), settle_account=VALUES(settle_account), status=VALUES(status), audit_status=VALUES(audit_status);

INSERT INTO ai_merchant_account(id, tenant_id, merchant_id, username, nickname, mobile, role_code, status)
VALUES
(1, 1, 1, 'merchant_admin', '商户管理员', '13800001002', 'MERCHANT_ADMIN', 'ENABLED'),
(2, 1, 1, 'merchant_ops', '商户运营', '13800001003', 'MERCHANT_OPS', 'ENABLED')
ON DUPLICATE KEY UPDATE nickname=VALUES(nickname), mobile=VALUES(mobile), role_code=VALUES(role_code), status=VALUES(status);

INSERT INTO ai_crm_customer(id, tenant_id, customer_no, customer_name, mobile, email, level, source, owner_user, status, next_follow_time)
VALUES
(1, 1, 'C-10001', '深圳智算科技', '13900000001', 'contact@zhisuan.com', 'A', 'EXHIBITION', 'admin', 'ACTIVE', '2026-05-25 10:00:00'),
(2, 1, 'C-10002', '广州云启信息', '13900000002', 'sales@yqxx.com', 'B', 'OPENAPI', 'ops', 'ACTIVE', '2026-05-23 15:00:00')
ON DUPLICATE KEY UPDATE customer_name=VALUES(customer_name), mobile=VALUES(mobile), email=VALUES(email), level=VALUES(level), source=VALUES(source), owner_user=VALUES(owner_user), status=VALUES(status), next_follow_time=VALUES(next_follow_time);

INSERT INTO ai_crm_follow_record(id, tenant_id, customer_id, follow_type, content, next_follow_time, create_by)
VALUES
(1, 1, 1, 'PHONE', '已完成首次电话沟通，客户对云主机有明确采购意向', '2026-05-25 10:00:00', 'admin'),
(2, 1, 2, 'VISIT', '已上门拜访，等待客户确认报价', '2026-05-23 15:00:00', 'ops')
ON DUPLICATE KEY UPDATE follow_type=VALUES(follow_type), content=VALUES(content), next_follow_time=VALUES(next_follow_time), create_by=VALUES(create_by);

INSERT INTO ai_crm_opportunity(id, tenant_id, customer_id, opportunity_no, name, stage, amount, expected_date, owner_user, status)
VALUES
(1, 1, 1, 'O-10001', '深圳智算科技云主机采购项目', 'QUOTE', 120000.00, '2026-06-15', 'admin', 'OPEN'),
(2, 1, 2, 'O-10002', '广州云启数据库升级项目', 'NEGOTIATION', 86000.00, '2026-06-10', 'ops', 'OPEN')
ON DUPLICATE KEY UPDATE name=VALUES(name), stage=VALUES(stage), amount=VALUES(amount), expected_date=VALUES(expected_date), owner_user=VALUES(owner_user), status=VALUES(status);


INSERT INTO ai_erp_inventory(id, tenant_id, sku_code, sku_name, warehouse_code, available_qty, locked_qty, unit_cost, safety_stock)
VALUES
(1, 1, 'SPU-10001', 'AI 云主机基础版', 'MAIN', 1000, 20, 66.00, 100),
(2, 1, 'SPU-10002', 'AI 云数据库套餐', 'MAIN', 500, 10, 128.00, 50)
ON DUPLICATE KEY UPDATE sku_name=VALUES(sku_name), available_qty=VALUES(available_qty), locked_qty=VALUES(locked_qty), unit_cost=VALUES(unit_cost), safety_stock=VALUES(safety_stock);

INSERT INTO ai_erp_stock_record(id, tenant_id, sku_code, warehouse_code, biz_type, biz_no, qty_delta, qty_after, remark, create_by)
VALUES
(1, 1, 'SPU-10001', 'MAIN', 'IN', 'IN-10001', 1000, 1000, '初始化入库', 'system'),
(2, 1, 'SPU-10002', 'MAIN', 'IN', 'IN-10002', 500, 500, '初始化入库', 'system')
ON DUPLICATE KEY UPDATE qty_delta=VALUES(qty_delta), qty_after=VALUES(qty_after), remark=VALUES(remark), create_by=VALUES(create_by);

INSERT INTO ai_erp_stock_check(id, tenant_id, check_no, warehouse_code, status, remark, create_by)
VALUES
(1, 1, 'CHK-10001', 'MAIN', 'FINISHED', '月度盘点完成', 'admin')
ON DUPLICATE KEY UPDATE status=VALUES(status), remark=VALUES(remark), create_by=VALUES(create_by);

INSERT INTO ai_bpm_process_definition(id, tenant_id, process_key, process_name, version_no, status, category, create_by)
VALUES
(1, 1, 'order_approval', '订单审批流程', 1, 'ACTIVE', 'TRADE', 'admin'),
(2, 1, 'merchant_onboarding', '商户入驻审批流程', 1, 'ACTIVE', 'MERCHANT', 'admin')
ON DUPLICATE KEY UPDATE process_name=VALUES(process_name), status=VALUES(status), category=VALUES(category), create_by=VALUES(create_by);

INSERT INTO ai_bpm_process_instance(id, tenant_id, instance_no, process_definition_id, process_key, business_id, starter, current_assignee, status)
VALUES
(1, 1, 'PI-10001', 1, 'order_approval', 'T62432C41F6624958', 'appuser', 'admin', 'RUNNING')
ON DUPLICATE KEY UPDATE business_id=VALUES(business_id), starter=VALUES(starter), current_assignee=VALUES(current_assignee), status=VALUES(status);

INSERT INTO ai_bpm_task(id, tenant_id, task_no, instance_id, task_name, assignee, status)
VALUES
(1, 1, 'TK-10001', 1, '审批订单', 'admin', 'TODO')
ON DUPLICATE KEY UPDATE task_name=VALUES(task_name), assignee=VALUES(assignee), status=VALUES(status);

INSERT INTO ai_infra_config(id, tenant_id, config_key, config_name, config_value, config_type, status, remark)
VALUES
(1, 1, 'system.site.name', '站点名称', 'AICloud 管理平台', 'SYSTEM', 1, '管理端展示名称'),
(2, 1, 'auth.password.maxRetry', '密码最大重试次数', '5', 'SECURITY', 1, '登录安全策略'),
(3, 1, 'openapi.sign.expireSeconds', '开放接口签名有效期', '300', 'OPENAPI', 1, '第三方 API 请求防重放窗口')
ON DUPLICATE KEY UPDATE config_name=VALUES(config_name), config_value=VALUES(config_value), config_type=VALUES(config_type), status=VALUES(status), remark=VALUES(remark);

INSERT INTO ai_trade_cart_item(id, tenant_id, user_id, spu_id, spu_name, price, quantity, selected)
VALUES
(1, 1, 3, 1, 'AI 云主机基础版', 99.00, 1, 1),
(2, 1, 3, 2, 'AI 云数据库套餐', 199.00, 1, 1)
ON DUPLICATE KEY UPDATE spu_name=VALUES(spu_name), price=VALUES(price), quantity=VALUES(quantity), selected=VALUES(selected);

INSERT INTO ai_trade_delivery(id, tenant_id, order_id, delivery_no, company_code, company_name, receiver_name, receiver_mobile, receiver_address, status, shipped_time)
VALUES
(1, 1, 1, 'D-10001', 'SF', '顺丰速运', 'APP用户', '13800000002', '广东省深圳市南山区科技园', 'SHIPPED', '2026-05-20 10:00:00')
ON DUPLICATE KEY UPDATE company_code=VALUES(company_code), company_name=VALUES(company_name), receiver_name=VALUES(receiver_name), receiver_mobile=VALUES(receiver_mobile), receiver_address=VALUES(receiver_address), status=VALUES(status), shipped_time=VALUES(shipped_time);

INSERT INTO ai_trade_after_sale(id, tenant_id, after_sale_no, order_id, user_id, type, reason, amount, status, audit_remark)
VALUES
(1, 1, 'AS-10001', 1, 3, 'REFUND', '演示退款申请', 10.00, 'APPLY', NULL)
ON DUPLICATE KEY UPDATE type=VALUES(type), reason=VALUES(reason), amount=VALUES(amount), status=VALUES(status), audit_remark=VALUES(audit_remark);

INSERT INTO ai_pay_channel(id, tenant_id, channel_code, channel_name, app_id, mch_id, notify_url, status, remark)
VALUES
(1, 1, 'MOCK', '模拟支付', 'mock-app', 'mock-mch', 'http://127.0.0.1:48080/pay/order/notify/success', 1, '本地开发模拟渠道'),
(2, 1, 'WECHAT', '微信支付', 'wx-demo', 'mch-demo', 'http://127.0.0.1:48080/pay/callback/wechat', 0, '预留微信支付渠道')
ON DUPLICATE KEY UPDATE channel_name=VALUES(channel_name), app_id=VALUES(app_id), mch_id=VALUES(mch_id), notify_url=VALUES(notify_url), status=VALUES(status), remark=VALUES(remark);

INSERT INTO ai_pay_refund(id, tenant_id, pay_order_id, trade_order_id, refund_no, channel, amount, reason, status)
VALUES
(1, 1, 1, 1, 'R-10001', 'MOCK', 10.00, '演示退款单', 'WAITING')
ON DUPLICATE KEY UPDATE channel=VALUES(channel), amount=VALUES(amount), reason=VALUES(reason), status=VALUES(status);

INSERT INTO ai_infra_file(id, tenant_id, file_name, file_url, file_type, file_size, storage, uploader, status)
VALUES
(1, 1, '演示头像.png', 'https://static.aicloud.local/demo/avatar.png', 'image/png', 20480, 'LOCAL', 'admin', 1),
(2, 1, '合同模板.pdf', 'https://static.aicloud.local/demo/contract.pdf', 'application/pdf', 102400, 'LOCAL', 'admin', 1)
ON DUPLICATE KEY UPDATE file_name=VALUES(file_name), file_url=VALUES(file_url), file_type=VALUES(file_type), file_size=VALUES(file_size), storage=VALUES(storage), uploader=VALUES(uploader), status=VALUES(status);

INSERT INTO ai_infra_job(id, tenant_id, job_name, handler_name, cron_expr, status, remark)
VALUES
(1, 1, '订单超时关闭', 'tradeOrderTimeoutJob', '0 */5 * * * ?', 1, '定时关闭超时未支付订单'),
(2, 1, '优惠券过期处理', 'couponExpireJob', '0 0 1 * * ?', 1, '每日处理过期优惠券')
ON DUPLICATE KEY UPDATE job_name=VALUES(job_name), handler_name=VALUES(handler_name), cron_expr=VALUES(cron_expr), status=VALUES(status), remark=VALUES(remark);

INSERT INTO ai_infra_notice(id, tenant_id, notice_type, title, content, receiver_type, receiver_id, status, publish_time, create_by)
VALUES
(1, 1, 'SYSTEM', '欢迎使用 AICloud', '这是系统初始化公告，可在管理端发布给全部用户。', 'ALL', NULL, 'PUBLISHED', '2026-05-21 10:00:00', 'admin'),
(2, 1, 'BUSINESS', '云主机活动上线', 'AI 云主机基础版限时优惠。', 'MEMBER', 3, 'DRAFT', NULL, 'admin')
ON DUPLICATE KEY UPDATE notice_type=VALUES(notice_type), title=VALUES(title), content=VALUES(content), receiver_type=VALUES(receiver_type), receiver_id=VALUES(receiver_id), status=VALUES(status), publish_time=VALUES(publish_time), create_by=VALUES(create_by);

INSERT INTO ai_openapi_app(id, tenant_id, app_key, app_secret, app_name, scopes, qps_limit, daily_limit, status, remark)
VALUES
(1, 1, 'demo-ak', 'demo-sk', '演示开放应用', 'member.read,order.read,pay.notify', 100, 20000, 1, '用于第三方 API 联调')
ON DUPLICATE KEY UPDATE app_name = VALUES(app_name), scopes = VALUES(scopes), status = VALUES(status);

INSERT INTO ai_openapi_call_log(id, tenant_id, app_key, api_path, method, request_id, request_ip, success, cost_ms, error_msg)
VALUES
(1, 1, 'demo-ak', '/openapi/v1/member/summary', 'GET', 'demo-request-001', '127.0.0.1', 1, 12, NULL)
ON DUPLICATE KEY UPDATE cost_ms = VALUES(cost_ms), success = VALUES(success);

INSERT INTO ai_openapi_webhook(id, tenant_id, app_key, event_type, target_url, secret, status, remark)
VALUES
(1, 1, 'demo-ak', 'PAY_SUCCESS', 'https://partner.example.com/webhook/pay', 'whsec_demo', 1, '支付成功通知')
ON DUPLICATE KEY UPDATE target_url = VALUES(target_url), status = VALUES(status);

INSERT INTO ai_mp_menu(id, tenant_id, name, menu_type, menu_key, url, parent_id, sort, status)
VALUES
(1, 1, '首页', 'VIEW', NULL, '/pages/index/index', 0, 10, 1),
(2, 1, '订单', 'VIEW', NULL, '/pages/order/list', 0, 20, 1),
(3, 1, '客服', 'CLICK', 'CONTACT_SERVICE', NULL, 0, 30, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort = VALUES(sort), status = VALUES(status);

INSERT INTO ai_mp_material(id, tenant_id, material_type, title, media_url, thumb_url, status)
VALUES
(1, 1, 'IMAGE', '首页活动图', 'https://static.aicloud.local/mp/banner-001.png', 'https://static.aicloud.local/mp/banner-001-thumb.png', 1),
(2, 1, 'ARTICLE', '会员权益说明', 'https://static.aicloud.local/mp/article-member.html', NULL, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), status = VALUES(status);

INSERT INTO ai_mp_message_template(id, tenant_id, template_code, template_name, content, status)
VALUES
(1, 1, 'ORDER_PAID', '订单支付成功通知', '您的订单已支付成功，请关注发货进度。', 1),
(2, 1, 'COUPON_RECEIVED', '优惠券到账通知', '您有一张新的优惠券已到账。', 1)
ON DUPLICATE KEY UPDATE template_name = VALUES(template_name), content = VALUES(content), status = VALUES(status);

INSERT INTO ai_mp_message_log(id, tenant_id, user_id, template_code, open_id, content, status)
VALUES
(1, 1, 3, 'ORDER_PAID', 'mp_openid_demo_001', '您的订单已支付成功，请关注发货进度。', 1)
ON DUPLICATE KEY UPDATE content = VALUES(content), status = VALUES(status);

-- 租户 2 商品演示数据：商品表在 business 脚本创建后再复制，避免 fresh init 顺序错误。
INSERT INTO ai_product_category(id, tenant_id, parent_id, name, sort, status)
SELECT id + 2000, 2, IF(parent_id = 0, 0, parent_id + 2000), CONCAT(name, '-T2'), sort, status FROM ai_product_category WHERE tenant_id = 1
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

INSERT INTO ai_product_spu(id, tenant_id, spu_no, name, sub_title, category_id, brand_name, unit_name, cover_url, sort, status, sale_price, stock)
SELECT id + 2000, 2, CONCAT(spu_no, '-T2'), CONCAT(name, '-租户2'), sub_title, category_id + 2000, brand_name, unit_name, cover_url, sort, status, sale_price, stock FROM ai_product_spu WHERE tenant_id = 1
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status), stock=VALUES(stock);


-- ===== AICloud 大而全基线补齐：扩展演示数据 =====
INSERT INTO ai_product_brand(id, tenant_id, brand_code, brand_name, logo_url, sort, status, remark) VALUES
(1,1,'AICLOUD','AICloud','https://static.aicloud.local/brand/aicloud.png',10,1,'自营云品牌')
ON DUPLICATE KEY UPDATE brand_name=VALUES(brand_name), status=VALUES(status);
INSERT INTO ai_product_sku(id, tenant_id, spu_id, sku_code, sku_name, spec_json, sale_price, market_price, cost_price, stock, weight, status) VALUES
(1,1,1,'SKU-10001-1C2G','AI 云主机 1C2G','{"cpu":"1C","memory":"2G"}',99.00,129.00,66.00,1000,0.000,1),
(2,1,1,'SKU-10001-2C4G','AI 云主机 2C4G','{"cpu":"2C","memory":"4G"}',199.00,259.00,120.00,500,0.000,1)
ON DUPLICATE KEY UPDATE sku_name=VALUES(sku_name), stock=VALUES(stock), status=VALUES(status);
INSERT INTO ai_product_property(id, tenant_id, property_code, property_name, input_type, sort, status) VALUES
(1,1,'cpu','CPU','SELECT',10,1),(2,1,'memory','内存','SELECT',20,1)
ON DUPLICATE KEY UPDATE property_name=VALUES(property_name), status=VALUES(status);
INSERT INTO ai_product_property_value(id, tenant_id, property_id, value_code, value_name, sort, status) VALUES
(1,1,1,'1C','1核',10,1),(2,1,1,'2C','2核',20,1),(3,1,2,'2G','2GB',10,1),(4,1,2,'4G','4GB',20,1)
ON DUPLICATE KEY UPDATE value_name=VALUES(value_name), status=VALUES(status);

INSERT INTO ai_promotion_activity(id, tenant_id, activity_no, activity_name, activity_type, start_time, end_time, status, remark) VALUES
(1,1,'ACT-10001','618 云资源钜惠','FULL_REDUCTION','2026-06-01 00:00:00','2026-06-18 23:59:59',1,'满减活动')
ON DUPLICATE KEY UPDATE activity_name=VALUES(activity_name), status=VALUES(status);
INSERT INTO ai_promotion_discount_rule(id, tenant_id, activity_id, rule_name, rule_type, threshold_amount, discount_amount, discount_rate, status) VALUES
(1,1,1,'满200减30','AMOUNT',200.00,30.00,100.00,1)
ON DUPLICATE KEY UPDATE rule_name=VALUES(rule_name), status=VALUES(status);
INSERT INTO ai_promotion_seckill(id, tenant_id, activity_id, spu_id, sku_id, seckill_price, stock, limit_count, start_time, end_time, status) VALUES
(1,1,1,1,1,79.00,100,1,'2026-06-18 10:00:00','2026-06-18 12:00:00',1)
ON DUPLICATE KEY UPDATE seckill_price=VALUES(seckill_price), stock=VALUES(stock), status=VALUES(status);

INSERT INTO ai_crm_contact(id, tenant_id, customer_id, contact_name, mobile, email, position_name, primary_status, status, remark) VALUES
(1,1,1,'李总','13900000011','boss@zhisuan.com','CEO',1,1,'关键决策人')
ON DUPLICATE KEY UPDATE contact_name=VALUES(contact_name), status=VALUES(status);
INSERT INTO ai_crm_contract(id, tenant_id, customer_id, opportunity_id, contract_no, contract_name, amount, sign_date, start_date, end_date, status, owner_user, remark) VALUES
(1,1,1,1,'HT-10001','智算云主机采购合同',120000.00,'2026-06-01','2026-06-01','2027-05-31','SIGNED','admin','年度合同')
ON DUPLICATE KEY UPDATE contract_name=VALUES(contract_name), amount=VALUES(amount), status=VALUES(status);
INSERT INTO ai_crm_receivable(id, tenant_id, contract_id, receivable_no, amount, received_amount, plan_date, receive_date, status, owner_user, remark) VALUES
(1,1,1,'AR-10001',60000.00,30000.00,'2026-06-30','2026-06-20','PARTIAL','admin','首期款')
ON DUPLICATE KEY UPDATE amount=VALUES(amount), received_amount=VALUES(received_amount), status=VALUES(status);
INSERT INTO ai_crm_customer_pool(id, tenant_id, customer_id, pool_reason, enter_time, claim_user, claim_time, status) VALUES
(1,1,2,'超过30天未跟进','2026-05-20 10:00:00',NULL,NULL,'IN_POOL')
ON DUPLICATE KEY UPDATE pool_reason=VALUES(pool_reason), status=VALUES(status);

INSERT INTO ai_erp_warehouse(id, tenant_id, warehouse_code, warehouse_name, contact_name, contact_mobile, address, status, remark) VALUES
(1,1,'MAIN','默认主仓','仓库管理员','13800003001','深圳南山云仓',1,'默认仓')
ON DUPLICATE KEY UPDATE warehouse_name=VALUES(warehouse_name), status=VALUES(status);
INSERT INTO ai_erp_supplier(id, tenant_id, supplier_code, supplier_name, contact_name, contact_mobile, contact_email, address, status, remark) VALUES
(1,1,'SUP-10001','深圳算力供应商','王经理','13800003002','sup@aicloud.local','深圳市南山区',1,'云资源供应商')
ON DUPLICATE KEY UPDATE supplier_name=VALUES(supplier_name), status=VALUES(status);
INSERT INTO ai_erp_purchase_order(id, tenant_id, purchase_no, supplier_id, warehouse_code, total_amount, status, create_by, audit_by, remark) VALUES
(1,1,'PO-10001',1,'MAIN',66000.00,'APPROVED','admin','admin','云主机资源采购')
ON DUPLICATE KEY UPDATE total_amount=VALUES(total_amount), status=VALUES(status);
INSERT INTO ai_erp_stock_transfer(id, tenant_id, transfer_no, sku_code, from_warehouse, to_warehouse, qty, status, create_by, remark) VALUES
(1,1,'TR-10001','SKU-10001-1C2G','MAIN','BACKUP',10,'DRAFT','admin','备用仓调拨')
ON DUPLICATE KEY UPDATE qty=VALUES(qty), status=VALUES(status);

INSERT INTO ai_merchant_shop(id, tenant_id, merchant_id, shop_no, shop_name, contact_name, contact_mobile, address, status, remark) VALUES
(1,1,1,'SHOP-10001','AICloud 深圳旗舰店','张店长','13800001004','深圳市南山区','ACTIVE','默认门店')
ON DUPLICATE KEY UPDATE shop_name=VALUES(shop_name), status=VALUES(status);
INSERT INTO ai_merchant_fee_rate(id, tenant_id, merchant_id, channel, fee_rate, settle_cycle, status, remark) VALUES
(1,1,1,'MOCK',0.0060,'T+1','ACTIVE','模拟渠道费率')
ON DUPLICATE KEY UPDATE fee_rate=VALUES(fee_rate), status=VALUES(status);
INSERT INTO ai_merchant_settlement(id, tenant_id, merchant_id, settlement_no, amount, fee_amount, settle_amount, status, settle_time, remark) VALUES
(1,1,1,'ST-10001',10000.00,60.00,9940.00,'WAITING',NULL,'待结算')
ON DUPLICATE KEY UPDATE amount=VALUES(amount), settle_amount=VALUES(settle_amount), status=VALUES(status);
INSERT INTO ai_merchant_withdraw(id, tenant_id, merchant_id, withdraw_no, amount, account_no, account_name, status, audit_remark) VALUES
(1,1,1,'WD-10001',5000.00,'6222000000000001','AICloud 深圳运营中心','APPLY',NULL)
ON DUPLICATE KEY UPDATE amount=VALUES(amount), status=VALUES(status);

INSERT INTO ai_member_points_rule(id, tenant_id, rule_code, rule_name, biz_type, points, growth, status, remark) VALUES
(1,1,'SIGN','每日签到','SIGN',10,1,1,'每日签到赠送积分')
ON DUPLICATE KEY UPDATE rule_name=VALUES(rule_name), status=VALUES(status);
INSERT INTO ai_member_sign_record(id, tenant_id, user_id, sign_date, points, continuous_days, remark) VALUES
(1,1,3,'2026-05-22',10,1,'签到赠送')
ON DUPLICATE KEY UPDATE points=VALUES(points), continuous_days=VALUES(continuous_days);
INSERT INTO ai_member_tag(id, tenant_id, tag_code, tag_name, tag_type, status, remark) VALUES
(1,1,'HIGH_VALUE','高价值客户','AUTO',1,'按消费自动计算')
ON DUPLICATE KEY UPDATE tag_name=VALUES(tag_name), status=VALUES(status);
INSERT INTO ai_member_growth_log(id, tenant_id, user_id, biz_type, growth_delta, growth_after, remark) VALUES
(1,1,3,'SIGN',1,1,'签到成长值')
ON DUPLICATE KEY UPDATE growth_delta=VALUES(growth_delta), growth_after=VALUES(growth_after);

INSERT INTO ai_bpm_model(id, tenant_id, model_key, model_name, model_json, version_no, status, create_by, remark) VALUES
(1,1,'order_audit','订单审批模型','{"nodes":["start","audit","end"]}',1,'ACTIVE','admin','演示模型')
ON DUPLICATE KEY UPDATE model_name=VALUES(model_name), version_no=VALUES(version_no), status=VALUES(status);
INSERT INTO ai_bpm_form(id, tenant_id, form_key, form_name, form_schema, status, create_by, remark) VALUES
(1,1,'order_audit_form','订单审批表单','{"fields":[{"label":"订单号","prop":"orderNo"}]}','ACTIVE','admin','演示表单')
ON DUPLICATE KEY UPDATE form_name=VALUES(form_name), status=VALUES(status);
INSERT INTO ai_bpm_cc(id, tenant_id, instance_id, task_id, user_id, username, read_status, read_time, remark) VALUES
(1,1,1,1,2,'ops',0,NULL,'流程抄送')
ON DUPLICATE KEY UPDATE username=VALUES(username), read_status=VALUES(read_status);

INSERT INTO ai_infra_codegen_table(id, tenant_id, table_name, class_name, module_name, business_name, author, status, remark) VALUES
(1,1,'ai_product_spu','ProductSpu','product','商品','admin',1,'代码生成元数据')
ON DUPLICATE KEY UPDATE class_name=VALUES(class_name), status=VALUES(status);
INSERT INTO ai_infra_codegen_column(id, tenant_id, table_id, column_name, java_field, java_type, query_type, form_show, list_show, sort) VALUES
(1,1,1,'spu_no','spuNo','String','LIKE',1,1,10),(2,1,1,'name','name','String','LIKE',1,1,20)
ON DUPLICATE KEY UPDATE java_field=VALUES(java_field), sort=VALUES(sort);
INSERT INTO ai_infra_storage_config(id, tenant_id, storage_code, storage_name, endpoint, bucket_name, access_key, secret_key, status, remark) VALUES
(1,1,'LOCAL','本地存储','http://127.0.0.1:48080','aicloud','local-ak','local-sk',1,'本地开发存储')
ON DUPLICATE KEY UPDATE storage_name=VALUES(storage_name), status=VALUES(status);
INSERT INTO ai_infra_job_log(id, tenant_id, job_id, job_name, handler_name, success, duration_ms, error_msg) VALUES
(1,1,1,'订单超时关闭','tradeOrderTimeoutJob',1,35,NULL)
ON DUPLICATE KEY UPDATE success=VALUES(success), duration_ms=VALUES(duration_ms);
