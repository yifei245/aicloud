package com.aicloud.module.crm.biz.service;

import com.aicloud.module.crm.biz.entity.AiCrmCustomer;
import com.aicloud.module.crm.biz.entity.AiCrmFollowRecord;
import com.aicloud.module.crm.biz.entity.AiCrmOpportunity;
import com.aicloud.module.crm.biz.mapper.CrmCustomerMapper;
import com.aicloud.module.crm.biz.mapper.CrmFollowRecordMapper;
import com.aicloud.module.crm.biz.mapper.CrmOpportunityMapper;
import com.aicloud.module.crm.biz.model.CrmCustomerSaveRequest;
import com.aicloud.module.crm.biz.model.CrmFollowSaveRequest;
import com.aicloud.module.crm.biz.model.CrmOpportunitySaveRequest;
import com.aicloud.module.crm.biz.model.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CrmService {

    private final CrmCustomerMapper customerMapper;
    private final CrmFollowRecordMapper followMapper;
    private final CrmOpportunityMapper opportunityMapper;

    public CrmService(CrmCustomerMapper customerMapper, CrmFollowRecordMapper followMapper,
            CrmOpportunityMapper opportunityMapper) {
        this.customerMapper = customerMapper;
        this.followMapper = followMapper;
        this.opportunityMapper = opportunityMapper;
    }

    public Map<String, Object> overview(Long tenantId) {
        List<AiCrmCustomer> customers = customerMapper.selectList(new LambdaQueryWrapper<AiCrmCustomer>()
                .eq(AiCrmCustomer::getTenantId, tenantId));
        List<AiCrmOpportunity> opportunities = opportunityMapper.selectList(new LambdaQueryWrapper<AiCrmOpportunity>()
                .eq(AiCrmOpportunity::getTenantId, tenantId));
        BigDecimal openAmount = opportunities.stream()
                .filter(item -> "OPEN".equals(item.getStatus()))
                .map(AiCrmOpportunity::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("customerCount", customers.size());
        data.put("activeCustomerCount", customers.stream().filter(item -> "ACTIVE".equals(item.getStatus())).count());
        data.put("opportunityCount", opportunities.size());
        data.put("openOpportunityAmount", openAmount);
        data.put("pendingFollowCount", customers.stream().filter(item -> item.getNextFollowTime() != null
                && !item.getNextFollowTime().isBefore(LocalDateTime.now())).count());
        return data;
    }

    public PageResponse<AiCrmCustomer> listCustomers(Long tenantId, String keyword, String status, String level,
            String source, String ownerUser, long pageNo, long pageSize) {
        Page<AiCrmCustomer> page = new Page<>(Math.max(pageNo, 1), Math.min(Math.max(pageSize, 1), 100));
        LambdaQueryWrapper<AiCrmCustomer> wrapper = new LambdaQueryWrapper<AiCrmCustomer>()
                .eq(AiCrmCustomer::getTenantId, tenantId)
                .eq(StringUtils.hasText(status), AiCrmCustomer::getStatus, status)
                .eq(StringUtils.hasText(level), AiCrmCustomer::getLevel, level)
                .eq(StringUtils.hasText(source), AiCrmCustomer::getSource, source)
                .eq(StringUtils.hasText(ownerUser), AiCrmCustomer::getOwnerUser, ownerUser)
                .and(StringUtils.hasText(keyword), query -> query
                        .like(AiCrmCustomer::getCustomerName, keyword)
                        .or()
                        .like(AiCrmCustomer::getCustomerNo, keyword)
                        .or()
                        .like(AiCrmCustomer::getMobile, keyword))
                .orderByDesc(AiCrmCustomer::getId);
        Page<AiCrmCustomer> result = customerMapper.selectPage(page, wrapper);
        PageResponse<AiCrmCustomer> data = new PageResponse<>();
        data.setTotal(result.getTotal());
        data.setPageNo(result.getCurrent());
        data.setPageSize(result.getSize());
        data.setList(result.getRecords());
        return data;
    }

    public AiCrmCustomer saveCustomer(CrmCustomerSaveRequest request) {
        AiCrmCustomer customer = request.getId() == null ? new AiCrmCustomer() : getCustomer(request.getId());
        customer.setTenantId(request.getTenantId());
        customer.setCustomerNo(StringUtils.hasText(request.getCustomerNo()) ? request.getCustomerNo() : "C-" + shortUuid());
        customer.setCustomerName(request.getCustomerName());
        customer.setMobile(request.getMobile());
        customer.setEmail(request.getEmail());
        customer.setLevel(StringUtils.hasText(request.getLevel()) ? request.getLevel() : "A");
        customer.setSource(StringUtils.hasText(request.getSource()) ? request.getSource() : "MANUAL");
        customer.setOwnerUser(request.getOwnerUser());
        customer.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "ACTIVE");
        customer.setNextFollowTime(request.getNextFollowTime());
        customer.setUpdateTime(LocalDateTime.now());
        if (customer.getId() == null) {
            customer.setCreateTime(LocalDateTime.now());
            customerMapper.insert(customer);
        } else {
            customerMapper.updateById(customer);
        }
        return customer;
    }

    public AiCrmCustomer getCustomer(Long id) {
        AiCrmCustomer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new IllegalArgumentException("客户不存在");
        }
        return customer;
    }

    public AiCrmCustomer updateCustomerStatus(Long id, String status) {
        AiCrmCustomer customer = getCustomer(id);
        customer.setStatus(status);
        customer.setUpdateTime(LocalDateTime.now());
        customerMapper.updateById(customer);
        return customer;
    }

    public AiCrmCustomer transferCustomer(Long id, String ownerUser) {
        AiCrmCustomer customer = getCustomer(id);
        customer.setOwnerUser(ownerUser);
        customer.setUpdateTime(LocalDateTime.now());
        customerMapper.updateById(customer);
        return customer;
    }

    public void deleteCustomer(Long id) {
        getCustomer(id);
        customerMapper.deleteById(id);
    }

    public List<AiCrmFollowRecord> listFollows(Long tenantId, Long customerId) {
        return followMapper.selectList(new LambdaQueryWrapper<AiCrmFollowRecord>()
                .eq(AiCrmFollowRecord::getTenantId, tenantId)
                .eq(AiCrmFollowRecord::getCustomerId, customerId)
                .orderByDesc(AiCrmFollowRecord::getId));
    }

    public AiCrmFollowRecord saveFollow(CrmFollowSaveRequest request) {
        if (customerMapper.selectById(request.getCustomerId()) == null) {
            throw new IllegalArgumentException("客户不存在");
        }
        AiCrmFollowRecord follow = new AiCrmFollowRecord();
        follow.setTenantId(request.getTenantId());
        follow.setCustomerId(request.getCustomerId());
        follow.setFollowType(request.getFollowType());
        follow.setContent(request.getContent());
        follow.setNextFollowTime(request.getNextFollowTime());
        follow.setCreateBy(request.getCreateBy());
        follow.setCreateTime(LocalDateTime.now());
        followMapper.insert(follow);

        AiCrmCustomer customer = customerMapper.selectById(request.getCustomerId());
        customer.setNextFollowTime(request.getNextFollowTime());
        customer.setUpdateTime(LocalDateTime.now());
        customerMapper.updateById(customer);
        return follow;
    }

    public List<AiCrmOpportunity> listOpportunities(Long tenantId, Long customerId, String stage, String status,
            String ownerUser) {
        return opportunityMapper.selectList(new LambdaQueryWrapper<AiCrmOpportunity>()
                .eq(AiCrmOpportunity::getTenantId, tenantId)
                .eq(customerId != null, AiCrmOpportunity::getCustomerId, customerId)
                .eq(StringUtils.hasText(stage), AiCrmOpportunity::getStage, stage)
                .eq(StringUtils.hasText(status), AiCrmOpportunity::getStatus, status)
                .eq(StringUtils.hasText(ownerUser), AiCrmOpportunity::getOwnerUser, ownerUser)
                .orderByDesc(AiCrmOpportunity::getId));
    }

    public AiCrmOpportunity getOpportunity(Long id) {
        AiCrmOpportunity opportunity = opportunityMapper.selectById(id);
        if (opportunity == null) {
            throw new IllegalArgumentException("商机不存在");
        }
        return opportunity;
    }

    public AiCrmOpportunity saveOpportunity(CrmOpportunitySaveRequest request) {
        if (customerMapper.selectById(request.getCustomerId()) == null) {
            throw new IllegalArgumentException("客户不存在");
        }
        AiCrmOpportunity opportunity = request.getId() == null ? new AiCrmOpportunity() : getOpportunity(request.getId());
        opportunity.setTenantId(request.getTenantId());
        opportunity.setCustomerId(request.getCustomerId());
        opportunity.setOpportunityNo(StringUtils.hasText(request.getOpportunityNo())
                ? request.getOpportunityNo() : "O-" + shortUuid());
        opportunity.setName(request.getName());
        opportunity.setStage(StringUtils.hasText(request.getStage()) ? request.getStage() : "INIT");
        opportunity.setAmount(request.getAmount() == null ? BigDecimal.ZERO : request.getAmount());
        opportunity.setExpectedDate(request.getExpectedDate());
        opportunity.setOwnerUser(request.getOwnerUser());
        opportunity.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "OPEN");
        opportunity.setUpdateTime(LocalDateTime.now());
        if (opportunity.getId() == null) {
            opportunity.setCreateTime(LocalDateTime.now());
            opportunityMapper.insert(opportunity);
        } else {
            opportunityMapper.updateById(opportunity);
        }
        return opportunity;
    }

    public AiCrmOpportunity updateOpportunityStage(Long id, String stage) {
        AiCrmOpportunity opportunity = getOpportunity(id);
        opportunity.setStage(stage);
        opportunity.setUpdateTime(LocalDateTime.now());
        opportunityMapper.updateById(opportunity);
        return opportunity;
    }

    public AiCrmOpportunity updateOpportunityStatus(Long id, String status) {
        AiCrmOpportunity opportunity = getOpportunity(id);
        opportunity.setStatus(status);
        opportunity.setUpdateTime(LocalDateTime.now());
        opportunityMapper.updateById(opportunity);
        return opportunity;
    }

    public void deleteOpportunity(Long id) {
        getOpportunity(id);
        opportunityMapper.deleteById(id);
    }

    private String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
