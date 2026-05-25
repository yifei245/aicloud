package com.aicloud.module.merchant.biz.service;

import com.aicloud.module.merchant.biz.entity.AiMerchantAccount;
import com.aicloud.module.merchant.biz.entity.AiMerchantProfile;
import com.aicloud.module.merchant.biz.mapper.MerchantAccountMapper;
import com.aicloud.module.merchant.biz.mapper.MerchantProfileMapper;
import com.aicloud.module.merchant.biz.model.MerchantAccountSaveRequest;
import com.aicloud.module.merchant.biz.model.MerchantSaveRequest;
import com.aicloud.common.pojo.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class MerchantService {

    private static final int OVERVIEW_MAP_SIZE = 5;

    private final MerchantProfileMapper profileMapper;
    private final MerchantAccountMapper accountMapper;

    public MerchantService(MerchantProfileMapper profileMapper, MerchantAccountMapper accountMapper) {
        this.profileMapper = profileMapper;
        this.accountMapper = accountMapper;
    }

    public Map<String, Object> overview(Long tenantId) {
        List<AiMerchantProfile> merchants = profileMapper.selectList(new LambdaQueryWrapper<AiMerchantProfile>()
                .eq(AiMerchantProfile::getTenantId, tenantId));
        List<AiMerchantAccount> accounts = accountMapper.selectList(new LambdaQueryWrapper<AiMerchantAccount>()
                .eq(AiMerchantAccount::getTenantId, tenantId));
        Map<String, Object> data = new HashMap<>(OVERVIEW_MAP_SIZE);
        data.put("merchantCount", merchants.size());
        data.put("enabledMerchantCount", merchants.stream().filter(item -> "ENABLED".equals(item.getStatus())).count());
        data.put("pendingAuditCount", merchants.stream().filter(item -> "PENDING".equals(item.getAuditStatus())).count());
        data.put("approvedAuditCount", merchants.stream().filter(item -> "APPROVED".equals(item.getAuditStatus())).count());
        data.put("accountCount", accounts.size());
        return data;
    }

    public PageResponse<AiMerchantProfile> listMerchants(Long tenantId, String keyword, String status,
            String auditStatus, long pageNo, long pageSize) {
        Page<AiMerchantProfile> page = new Page<>(Math.max(pageNo, 1), Math.min(Math.max(pageSize, 1), 100));
        LambdaQueryWrapper<AiMerchantProfile> wrapper = new LambdaQueryWrapper<AiMerchantProfile>()
                .eq(AiMerchantProfile::getTenantId, tenantId)
                .eq(StringUtils.hasText(status), AiMerchantProfile::getStatus, status)
                .eq(StringUtils.hasText(auditStatus), AiMerchantProfile::getAuditStatus, auditStatus)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(AiMerchantProfile::getMerchantName, keyword)
                        .or()
                        .like(AiMerchantProfile::getMerchantNo, keyword)
                        .or()
                        .like(AiMerchantProfile::getContactMobile, keyword))
                .orderByDesc(AiMerchantProfile::getId);
        Page<AiMerchantProfile> result = profileMapper.selectPage(page, wrapper);
        PageResponse<AiMerchantProfile> data = new PageResponse<>();
        data.setTotal(result.getTotal());
        data.setPageNo(result.getCurrent());
        data.setPageSize(result.getSize());
        data.setList(result.getRecords());
        return data;
    }

    public AiMerchantProfile getMerchant(Long id) {
        AiMerchantProfile merchant = profileMapper.selectById(id);
        if (merchant == null) {
            throw new IllegalArgumentException("商户不存在");
        }
        return merchant;
    }

    public AiMerchantProfile saveMerchant(MerchantSaveRequest request) {
        AiMerchantProfile merchant = request.getId() == null ? new AiMerchantProfile() : getMerchant(request.getId());
        merchant.setTenantId(request.getTenantId());
        merchant.setMerchantNo(StringUtils.hasText(request.getMerchantNo()) ? request.getMerchantNo() : "M-" + shortUuid());
        merchant.setMerchantName(request.getMerchantName());
        merchant.setContactName(request.getContactName());
        merchant.setContactMobile(request.getContactMobile());
        merchant.setContactEmail(request.getContactEmail());
        merchant.setSettleAccount(request.getSettleAccount());
        merchant.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ENABLED");
        merchant.setAuditStatus(StringUtils.hasText(request.getAuditStatus()) ? request.getAuditStatus() : "PENDING");
        merchant.setUpdateTime(LocalDateTime.now());
        if (merchant.getId() == null) {
            merchant.setCreateTime(LocalDateTime.now());
            profileMapper.insert(merchant);
        } else {
            profileMapper.updateById(merchant);
        }
        return merchant;
    }

    public AiMerchantProfile auditMerchant(Long id, String auditStatus) {
        AiMerchantProfile merchant = getMerchant(id);
        merchant.setAuditStatus(auditStatus);
        merchant.setUpdateTime(LocalDateTime.now());
        profileMapper.updateById(merchant);
        return merchant;
    }

    public AiMerchantProfile updateMerchantStatus(Long id, String status) {
        AiMerchantProfile merchant = getMerchant(id);
        merchant.setStatus(status);
        merchant.setUpdateTime(LocalDateTime.now());
        profileMapper.updateById(merchant);
        return merchant;
    }

    public void deleteMerchant(Long id) {
        getMerchant(id);
        Long accountCount = accountMapper.selectCount(new LambdaQueryWrapper<AiMerchantAccount>()
                .eq(AiMerchantAccount::getMerchantId, id));
        if (accountCount > 0) {
            throw new IllegalArgumentException("商户存在账号，不能删除");
        }
        profileMapper.deleteById(id);
    }

    public List<AiMerchantAccount> listAccounts(Long tenantId, Long merchantId, String status) {
        return accountMapper.selectList(new LambdaQueryWrapper<AiMerchantAccount>()
                .eq(AiMerchantAccount::getTenantId, tenantId)
                .eq(merchantId != null, AiMerchantAccount::getMerchantId, merchantId)
                .eq(StringUtils.hasText(status), AiMerchantAccount::getStatus, status)
                .orderByDesc(AiMerchantAccount::getId));
    }

    public AiMerchantAccount getAccount(Long id) {
        AiMerchantAccount account = accountMapper.selectById(id);
        if (account == null) {
            throw new IllegalArgumentException("商户账号不存在");
        }
        return account;
    }

    public AiMerchantAccount saveAccount(MerchantAccountSaveRequest request) {
        if (profileMapper.selectById(request.getMerchantId()) == null) {
            throw new IllegalArgumentException("商户不存在");
        }
        AiMerchantAccount account = request.getId() == null ? new AiMerchantAccount() : getAccount(request.getId());
        account.setTenantId(request.getTenantId());
        account.setMerchantId(request.getMerchantId());
        account.setUsername(request.getUsername());
        account.setNickname(request.getNickname());
        account.setMobile(request.getMobile());
        account.setRoleCode(StringUtils.hasText(request.getRoleCode()) ? request.getRoleCode() : "MERCHANT_OPS");
        account.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ENABLED");
        account.setUpdateTime(LocalDateTime.now());
        if (account.getId() == null) {
            account.setCreateTime(LocalDateTime.now());
            accountMapper.insert(account);
        } else {
            accountMapper.updateById(account);
        }
        return account;
    }

    public AiMerchantAccount updateAccountStatus(Long id, String status) {
        AiMerchantAccount account = getAccount(id);
        account.setStatus(status);
        account.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(account);
        return account;
    }

    public void deleteAccount(Long id) {
        getAccount(id);
        accountMapper.deleteById(id);
    }

    private String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
