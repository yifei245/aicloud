package com.aicloud.module.fms.service.voucher;

import com.aicloud.framework.common.enums.CommonStatusEnum;
import com.aicloud.framework.common.util.object.BeanUtils;
import com.aicloud.framework.mybatis.core.integration.IntegrationInboxService;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.fms.api.business.FmsBusinessApi;
import com.aicloud.module.fms.controller.admin.voucher.vo.*;
import com.aicloud.module.fms.dal.dataobject.voucher.FmsVoucherDO;
import com.aicloud.module.fms.enums.voucher.FmsVoucherStatusEnum;
import com.aicloud.module.fms.service.config.*;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** Business facts generate reviewed-by-human drafts under an explicit account/subject rule. */
@Service
public class FmsBusinessService {
    @Resource private JdbcTemplate jdbc;
    @Resource private IntegrationInboxService inbox;
    @Resource private FmsAccountSetService accounts;
    @Resource private FmsSubjectService subjects;
    @Resource private FmsVoucherWordService words;
    @Resource private FmsVoucherService vouchers;

    @Transactional(rollbackFor = Exception.class)
    public void configure(Rule rule, Long userId) {
        accounts.validateAccountSetWritePermission(rule.getAccountSetId(), userId);
        words.validateVoucherWordExists(rule.getAccountSetId(), rule.getVoucherWordId());
        if (rule.getDebitSubjectId().equals(rule.getCreditSubjectId())) throw new IllegalArgumentException("借贷科目不能相同");
        for (Long id : List.of(rule.getDebitSubjectId(), rule.getCreditSubjectId())) {
            var subject = subjects.getSubject(rule.getAccountSetId(), id, userId);
            if (subject == null || !CommonStatusEnum.isEnable(subject.getStatus())) throw new IllegalArgumentException("凭证来源科目不存在或已停用");
        }
        // The configuring accountant supplies the posting authority, never an arbitrary caller-selected user ID.
        jdbc.update("INSERT INTO fms_business_rule(tenant_id,source,account_set_id,voucher_word_id,debit_subject_id,credit_subject_id,posting_user_id) VALUES(?,?,?,?,?,?,?)"
                        + " ON DUPLICATE KEY UPDATE account_set_id=VALUES(account_set_id),voucher_word_id=VALUES(voucher_word_id),debit_subject_id=VALUES(debit_subject_id),credit_subject_id=VALUES(credit_subject_id),posting_user_id=VALUES(posting_user_id)",
                tenant(), rule.getSource(), rule.getAccountSetId(), rule.getVoucherWordId(), rule.getDebitSubjectId(), rule.getCreditSubjectId(), userId);
    }
    public List<Map<String, Object>> rules(Long accountSetId, Long userId) {
        accounts.validateAccountSetReadPermission(accountSetId, userId);
        return jdbc.queryForList("SELECT source,account_set_id,voucher_word_id,debit_subject_id,credit_subject_id,posting_user_id FROM fms_business_rule WHERE tenant_id=? AND account_set_id=?", tenant(), accountSetId);
    }
    public List<Map<String, Object>> sources(Long accountSetId, Long userId) {
        accounts.validateAccountSetReadPermission(accountSetId, userId);
        return jdbc.queryForList("SELECT id,source,document_id,document_no,amount,voucher_id,reversal_voucher_id,state,create_time FROM fms_business_source WHERE tenant_id=? AND account_set_id=? ORDER BY id DESC LIMIT 500", tenant(), accountSetId);
    }
    public Long deliver(FmsBusinessApi.Event event) {
        return inbox.execute(tenant(), "fms-business", event.getBusinessKey(), event, Long.class, () -> {
            var rules = jdbc.queryForList("SELECT * FROM fms_business_rule WHERE tenant_id=? AND source=?", tenant(), event.getSource());
            if (rules.isEmpty()) throw new IllegalArgumentException("尚未配置财务来源规则：" + event.getSource());
            Map<String, Object> rule = rules.getFirst();
            Long account = number(rule, "account_set_id"), user = number(rule, "posting_user_id");
            accounts.validateAccountSetWritePermission(account, user);
            accounts.lockAccountSet(account);
            var active = jdbc.queryForList("SELECT * FROM fms_business_source WHERE tenant_id=? AND source=? AND document_id=? AND state='ACTIVE' FOR UPDATE",
                    tenant(), event.getSource(), event.getDocumentId());
            if ("CANCEL".equals(event.getAction())) return cancel(event, active);
            if (!"CREATE".equals(event.getAction())) throw new IllegalArgumentException("未知财务来源动作");
            BigDecimal signed = event.isReverse() ? event.getAmount().negate() : event.getAmount();
            if (!active.isEmpty()) {
                if (((BigDecimal) active.getFirst().get("amount")).compareTo(signed) != 0) throw new IllegalArgumentException("此业务已有凭证，修改金额必须先撤销原业务");
                return number(active.getFirst(), "voucher_id");
            }
            Long debit = number(rule, event.isReverse() ? "credit_subject_id" : "debit_subject_id");
            Long credit = number(rule, event.isReverse() ? "debit_subject_id" : "credit_subject_id");
            String digest = event.getSource() + "/" + event.getDocumentNo() + (event.getSummary() == null ? "" : " " + event.getSummary());
            List<FmsVoucherEntrySaveReqVO> entries = List.of(
                    new FmsVoucherEntrySaveReqVO().setSubjectId(debit).setDigest(digest).setDebitAmount(event.getAmount()).setCreditAmount(BigDecimal.ZERO),
                    new FmsVoucherEntrySaveReqVO().setSubjectId(credit).setDigest(digest).setDebitAmount(BigDecimal.ZERO).setCreditAmount(event.getAmount()));
            Long voucher = create(account, number(rule, "voucher_word_id"), event.getOccurredAt(), user, entries);
            jdbc.update("INSERT INTO fms_business_source(tenant_id,source,document_id,business_key,document_no,amount,account_set_id,posting_user_id,voucher_id,state) VALUES(?,?,?,?,?,?,?,?,?,'ACTIVE')",
                    tenant(), event.getSource(), event.getDocumentId(), event.getBusinessKey(), event.getDocumentNo(), signed, account, user, voucher);
            return voucher;
        });
    }
    private Long cancel(FmsBusinessApi.Event event, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) return 0L;
        Map<String, Object> original = rows.getFirst();
        Long account = number(original, "account_set_id"), user = number(original, "posting_user_id"), id = number(original, "voucher_id");
        accounts.validateAccountSetWritePermission(account, user);
        accounts.lockAccountSet(account);
        FmsVoucherDO voucher = vouchers.getVoucher(account, id, user);
        Long reversal = null;
        if (FmsVoucherStatusEnum.APPROVED.getStatus().equals(voucher.getStatus())) {
            var entries = vouchers.getVoucherEntryList(List.of(id)).stream().map(entry -> {
                var reversed = BeanUtils.toBean(entry, FmsVoucherEntrySaveReqVO.class);
                reversed.setId(null); reversed.setDigest("冲回 " + event.getDocumentNo() + "：" + entry.getDigest());
                reversed.setDebitAmount(entry.getCreditAmount()); reversed.setCreditAmount(entry.getDebitAmount());
                return reversed;
            }).toList();
            reversal = create(account, voucher.getVoucherWordId(), event.getOccurredAt(), user, entries);
        } else {
            vouchers.deleteVoucherList(account, List.of(id), user);
        }
        jdbc.update("UPDATE fms_business_source SET state='CANCELLED',reversal_voucher_id=?,update_time=CURRENT_TIMESTAMP WHERE id=? AND tenant_id=?", reversal, original.get("id"), tenant());
        return reversal == null ? id : reversal;
    }
    private Long create(Long account, Long word, LocalDateTime time, Long user, List<FmsVoucherEntrySaveReqVO> entries) {
        return vouchers.createVoucher(new FmsVoucherSaveReqVO().setAccountSetId(account).setVoucherWordId(word)
                .setVoucherTime(time).setAttachmentCount(0).setEntries(entries)
                .setVoucherNumber(vouchers.getNextVoucherNumber(account, word, time, user)), user);
    }
    private static Long number(Map<String, Object> row, String key) { return ((Number) row.get(key)).longValue(); }
    private static Long tenant() { return TenantContextHolder.getRequiredTenantId(); }
    @Data public static class Rule {
        @NotBlank @Pattern(regexp = "ERP_RECEIPT|ERP_PAYMENT|MALL_PAYMENT|MALL_REFUND|HRM_SALARY|OA_EXPENSE|OA_TRAVEL_EXPENSE") private String source;
        @NotNull @Positive private Long accountSetId;
        @NotNull @Positive private Long voucherWordId;
        @NotNull @Positive private Long debitSubjectId;
        @NotNull @Positive private Long creditSubjectId;
    }
}
