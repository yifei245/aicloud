package com.aicloud.module.report.biz.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Mapper
public interface ReportDashboardMapper {

        /**
     * Query order statistics.
     *
     * @param tenantId tenant id
     * @return statistics rows
     */
@Select("SELECT COUNT(*) orderCount, COALESCE(SUM(pay_amount),0) gmv FROM ai_trade_order WHERE tenant_id = #{tenantId}")
    Map<String, Object> orderStats(Long tenantId);

        /**
     * Query order status distribution.
     *
     * @param tenantId tenant id
     * @return distribution rows
     */
@Select("SELECT status, COUNT(*) count, COALESCE(SUM(pay_amount),0) amount FROM ai_trade_order WHERE tenant_id = #{tenantId} GROUP BY status ORDER BY count DESC")
    List<Map<String, Object>> orderStatusStats(Long tenantId);

        /**
     * Query payment statistics.
     *
     * @param tenantId tenant id
     * @return statistics rows
     */
@Select("SELECT COUNT(*) payCount, COALESCE(SUM(amount),0) payAmount FROM ai_pay_order WHERE tenant_id = #{tenantId} AND status = 'SUCCESS'")
    Map<String, Object> payStats(Long tenantId);

        /**
     * Query payment status distribution.
     *
     * @param tenantId tenant id
     * @return distribution rows
     */
@Select("SELECT status, COUNT(*) count, COALESCE(SUM(amount),0) amount FROM ai_pay_order WHERE tenant_id = #{tenantId} GROUP BY status ORDER BY count DESC")
    List<Map<String, Object>> payStatusStats(Long tenantId);

        /**
     * Query member statistics.
     *
     * @param tenantId tenant id
     * @return statistics rows
     */
@Select("SELECT COUNT(*) memberCount, COALESCE(SUM(points),0) totalPoints, COALESCE(SUM(balance),0) totalBalance FROM ai_member_profile WHERE tenant_id = #{tenantId}")
    Map<String, Object> memberStats(Long tenantId);

        /**
     * Query member level distribution.
     *
     * @param tenantId tenant id
     * @return distribution rows
     */
@Select("SELECT level, COUNT(*) count FROM ai_member_profile WHERE tenant_id = #{tenantId} GROUP BY level ORDER BY count DESC")
    List<Map<String, Object>> memberLevelStats(Long tenantId);

        /**
     * Query customer statistics.
     *
     * @param tenantId tenant id
     * @return statistics rows
     */
@Select("SELECT COUNT(*) customerCount FROM ai_crm_customer WHERE tenant_id = #{tenantId}")
    Map<String, Object> customerStats(Long tenantId);

        /**
     * Query opportunity stage distribution.
     *
     * @param tenantId tenant id
     * @return distribution rows
     */
@Select("SELECT stage, COUNT(*) count, COALESCE(SUM(amount),0) amount FROM ai_crm_opportunity WHERE tenant_id = #{tenantId} GROUP BY stage ORDER BY count DESC")
    List<Map<String, Object>> opportunityStageStats(Long tenantId);

        /**
     * Query merchant statistics.
     *
     * @param tenantId tenant id
     * @return statistics rows
     */
@Select("SELECT COUNT(*) merchantCount, SUM(CASE WHEN audit_status = 'PENDING' THEN 1 ELSE 0 END) pendingAuditCount, SUM(CASE WHEN status = 'ENABLED' THEN 1 ELSE 0 END) enabledMerchantCount FROM ai_merchant_profile WHERE tenant_id = #{tenantId}")
    Map<String, Object> merchantStats(Long tenantId);

        /**
     * Query inventory statistics.
     *
     * @param tenantId tenant id
     * @return statistics rows
     */
@Select("SELECT COUNT(*) skuCount, COALESCE(SUM(available_qty),0) availableQty, COALESCE(SUM(locked_qty),0) lockedQty, SUM(CASE WHEN available_qty <= safety_stock THEN 1 ELSE 0 END) lowStockCount FROM ai_erp_inventory WHERE tenant_id = #{tenantId}")
    Map<String, Object> inventoryStats(Long tenantId);

        /**
     * Query product statistics.
     *
     * @param tenantId tenant id
     * @return statistics rows
     */
@Select("SELECT COUNT(*) productCount, COALESCE(SUM(stock),0) productStock FROM ai_product_spu WHERE tenant_id = #{tenantId}")
    Map<String, Object> productStats(Long tenantId);
}
