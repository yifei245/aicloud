package com.aicloud.module.system.biz.service;

import com.aicloud.module.system.biz.entity.AiDictData;
import com.aicloud.module.system.biz.entity.AiDictType;
import com.aicloud.module.system.biz.mapper.DictDataMapper;
import com.aicloud.module.system.biz.mapper.DictTypeMapper;
import com.aicloud.module.system.biz.model.dict.DictDataSaveRequest;
import com.aicloud.module.system.biz.model.dict.DictTypeSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class DictAdminService {

    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;

    public DictAdminService(DictTypeMapper dictTypeMapper, DictDataMapper dictDataMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
    }

    public List<AiDictType> listTypes(Long tenantId, String keyword, Integer status) {
        return dictTypeMapper.selectList(new LambdaQueryWrapper<AiDictType>()
                .eq(AiDictType::getTenantId, tenantId)
                .and(StringUtils.hasText(keyword), w -> w.like(AiDictType::getDictType, keyword).or().like(AiDictType::getDictName, keyword))
                .eq(status != null, AiDictType::getStatus, status)
                .orderByAsc(AiDictType::getId));
    }

    public List<AiDictData> listData(Long tenantId, String dictType, Integer status) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<AiDictData>()
                .eq(AiDictData::getTenantId, tenantId)
                .eq(StringUtils.hasText(dictType), AiDictData::getDictType, dictType)
                .eq(status != null, AiDictData::getStatus, status)
                .orderByAsc(AiDictData::getSort, AiDictData::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiDictType saveType(Long tenantId, DictTypeSaveRequest request) {
        if (!StringUtils.hasText(request.getDictType()) || !StringUtils.hasText(request.getDictName())) {
            throw new IllegalArgumentException("字典类型和值名称不能为空");
        }
        if (dictTypeMapper.countByType(tenantId, request.getDictType(), request.getId()) > 0) {
            throw new IllegalArgumentException("字典类型已存在");
        }
        AiDictType entity = request.getId() == null ? new AiDictType() : dictTypeMapper.selectById(request.getId());
        if (entity == null) {
            throw new IllegalArgumentException("字典类型不存在");
        }
        if (request.getId() == null) {
            entity.setTenantId(tenantId);
            entity.setCreateTime(LocalDateTime.now());
        }
        entity.setDictType(request.getDictType());
        entity.setDictName(request.getDictName());
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        entity.setUpdateTime(LocalDateTime.now());
        if (request.getId() == null) {
            dictTypeMapper.insert(entity);
        } else {
            dictTypeMapper.updateById(entity);
        }
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public AiDictData saveData(Long tenantId, DictDataSaveRequest request) {
        if (!StringUtils.hasText(request.getDictType()) || !StringUtils.hasText(request.getDictLabel()) || !StringUtils.hasText(request.getDictValue())) {
            throw new IllegalArgumentException("字典数据字段不能为空");
        }
        AiDictData entity = request.getId() == null ? new AiDictData() : dictDataMapper.selectById(request.getId());
        if (entity == null) {
            throw new IllegalArgumentException("字典数据不存在");
        }
        if (request.getId() == null) {
            entity.setTenantId(tenantId);
            entity.setCreateTime(LocalDateTime.now());
        }
        entity.setDictType(request.getDictType());
        entity.setDictLabel(request.getDictLabel());
        entity.setDictValue(request.getDictValue());
        entity.setSort(request.getSort() == null ? 0 : request.getSort());
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        entity.setUpdateTime(LocalDateTime.now());
        if (request.getId() == null) {
            dictDataMapper.insert(entity);
        } else {
            dictDataMapper.updateById(entity);
        }
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long tenantId, Long id) {
        AiDictType type = dictTypeMapper.selectById(id);
        if (type == null || !tenantId.equals(type.getTenantId())) {
            return;
        }
        dictDataMapper.delete(new LambdaQueryWrapper<AiDictData>()
                .eq(AiDictData::getTenantId, tenantId)
                .eq(AiDictData::getDictType, type.getDictType()));
        dictTypeMapper.deleteById(id);
    }

    public void deleteData(Long tenantId, Long id) {
        dictDataMapper.delete(new LambdaQueryWrapper<AiDictData>()
                .eq(AiDictData::getTenantId, tenantId)
                .eq(AiDictData::getId, id));
    }
}
