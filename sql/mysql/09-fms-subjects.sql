USE aicloud;

-- Root subject codes: Ministry of Finance Small Enterprise Accounting Standards appendix.

-- https://kjs.mof.gov.cn/zhengcefabu/201111/P020111118325852734144.pdf

-- Detail subjects support the existing report and closing presets; users may customize them.

-- Repeat execution preserves all existing subject templates and IDs.

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1001','库存现金',0,1,1,1,1,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1001' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1002','银行存款',0,1,1,1,1,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1002' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1012','其他货币资金',0,1,1,1,1,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1012' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1101','短期投资',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1101' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1121','应收票据',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1121' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1122','应收账款',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1122' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1123','预付账款',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1123' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1131','应收股利',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1131' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1132','应收利息',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1132' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1221','其他应收款',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1221' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1401','材料采购',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1401' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1402','在途物资',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1402' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1403','原材料',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1403' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1404','材料成本差异',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1404' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1405','库存商品',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1405' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1407','商品进销差价',0,1,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1407' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1408','委托加工物资',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1408' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1411','周转材料',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1411' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1421','消耗性生物资产',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1421' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1501','长期债券投资',0,1,2,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1501' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1511','长期股权投资',0,1,2,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1511' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1601','固定资产',0,1,2,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1601' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1602','累计折旧',0,1,2,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1602' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1604','在建工程',0,1,2,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1604' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1605','工程物资',0,1,2,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1605' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1606','固定资产清理',0,1,2,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1606' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1621','生产性生物资产',0,1,2,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1621' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1622','生产性生物资产累计折旧',0,1,2,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1622' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1701','无形资产',0,1,2,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1701' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1702','累计摊销',0,1,2,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1702' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1801','长期待摊费用',0,1,2,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1801' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '1901','待处理财产损溢',0,1,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='1901' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2001','短期借款',0,2,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2001' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2201','应付票据',0,2,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2201' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2202','应付账款',0,2,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2202' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2203','预收账款',0,2,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2203' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2211','应付职工薪酬',0,2,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2211' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2221','应交税费',0,2,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2221' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222101','应交增值税',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222101' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '22210101','进项税额',(SELECT id FROM fms_subject_template WHERE code='222101' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,3,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='22210101' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '22210104','转出未交增值税',(SELECT id FROM fms_subject_template WHERE code='222101' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,3,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='22210104' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '22210107','销项税额',(SELECT id FROM fms_subject_template WHERE code='222101' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,3,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='22210107' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222102','未交增值税',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222102' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222104','待抵扣进项税额',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222104' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222105','待认证进项税额',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222105' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222106','待转销项税额',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222106' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222107','增值税留抵税额',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222107' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222111','应交所得税',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222111' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222113','应交教育费附加',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222113' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222114','应交地方教育费附加',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222114' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222117','应交城市维护建设税',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222117' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '222121','应交消费税',(SELECT id FROM fms_subject_template WHERE code='2221' AND deleted=0 ORDER BY id LIMIT 1),2,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='222121' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2231','应付利息',0,2,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2231' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2232','应付利润',0,2,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2232' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2241','其他应付款',0,2,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2241' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2401','递延收益',0,2,2,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2401' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2501','长期借款',0,2,2,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2501' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '2701','长期应付款',0,2,2,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='2701' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '3001','实收资本',0,3,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='3001' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '3002','资本公积',0,3,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='3002' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '3101','盈余公积',0,3,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='3101' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '3103','本年利润',0,3,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='3103' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '3104','利润分配',0,3,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='3104' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '310415','未分配利润',(SELECT id FROM fms_subject_template WHERE code='3104' AND deleted=0 ORDER BY id LIMIT 1),3,1,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='310415' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '4001','生产成本',0,4,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='4001' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '4101','制造费用',0,4,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='4101' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '4301','研发支出',0,4,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='4301' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '4401','工程施工',0,4,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='4401' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '4403','机械作业',0,4,1,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='4403' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5001','主营业务收入',0,5,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5001' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5051','其他业务收入',0,5,1,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5051' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5111','投资收益',0,5,2,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5111' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5301','营业外收入',0,5,2,2,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5301' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '530102','政府补助',(SELECT id FROM fms_subject_template WHERE code='5301' AND deleted=0 ORDER BY id LIMIT 1),5,2,2,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='530102' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5401','主营业务成本',0,5,5,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5401' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5402','其他业务成本',0,5,5,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5402' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5403','税金及附加',0,5,5,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5403' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540301','消费税',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540301' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540303','城市维护建设税',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540303' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540304','资源税',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540304' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540305','土地增值税',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540305' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540306','城镇土地使用税',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540306' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540307','房产税',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540307' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540308','车船税',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540308' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540309','印花税',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540309' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540310','教育费附加',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540310' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540311','矿产资源补偿费',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540311' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540312','排污费',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540312' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '540313','地方教育费附加',(SELECT id FROM fms_subject_template WHERE code='5403' AND deleted=0 ORDER BY id LIMIT 1),5,5,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='540313' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5601','销售费用',0,5,3,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5601' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560101','通讯费',(SELECT id FROM fms_subject_template WHERE code='5601' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560101' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560102','房租水电费',(SELECT id FROM fms_subject_template WHERE code='5601' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560102' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560107','差旅费',(SELECT id FROM fms_subject_template WHERE code='5601' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560107' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560110','工资',(SELECT id FROM fms_subject_template WHERE code='5601' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560110' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560115','商品维修费',(SELECT id FROM fms_subject_template WHERE code='5601' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560115' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560116','广告和业务宣传费',(SELECT id FROM fms_subject_template WHERE code='5601' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560116' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5602','管理费用',0,5,3,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5602' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560205','交际应酬费',(SELECT id FROM fms_subject_template WHERE code='5602' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560205' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560209','工资',(SELECT id FROM fms_subject_template WHERE code='5602' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560209' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560210','业务招待费',(SELECT id FROM fms_subject_template WHERE code='5602' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560210' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560213','开办费',(SELECT id FROM fms_subject_template WHERE code='5602' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560213' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560215','研究费用',(SELECT id FROM fms_subject_template WHERE code='5602' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560215' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5603','财务费用',0,5,3,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5603' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560302','利息费用',(SELECT id FROM fms_subject_template WHERE code='5603' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560302' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '560303','手续费',(SELECT id FROM fms_subject_template WHERE code='5603' AND deleted=0 ORDER BY id LIMIT 1),5,3,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='560303' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5711','营业外支出',0,5,4,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5711' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '571103','坏账损失',(SELECT id FROM fms_subject_template WHERE code='5711' AND deleted=0 ORDER BY id LIMIT 1),5,4,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='571103' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '571104','无法收回的长期债券投资损失',(SELECT id FROM fms_subject_template WHERE code='5711' AND deleted=0 ORDER BY id LIMIT 1),5,4,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='571104' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '571105','无法收回的长期股权投资损失',(SELECT id FROM fms_subject_template WHERE code='5711' AND deleted=0 ORDER BY id LIMIT 1),5,4,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='571105' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '571106','自然灾害等不可抗力造成的损失',(SELECT id FROM fms_subject_template WHERE code='5711' AND deleted=0 ORDER BY id LIMIT 1),5,4,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='571106' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '571107','税收滞纳金',(SELECT id FROM fms_subject_template WHERE code='5711' AND deleted=0 ORDER BY id LIMIT 1),5,4,1,0,0,2,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='571107' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '5801','所得税费用',0,5,7,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='5801' AND deleted=0);

INSERT INTO fms_subject_template (code,name,parent_id,type,category,balance_direction,cash,status,level,quantity_accounting)
SELECT '6000','以前年度损益调整',0,5,6,1,0,0,1,0
WHERE NOT EXISTS (SELECT 1 FROM fms_subject_template WHERE code='6000' AND deleted=0);
