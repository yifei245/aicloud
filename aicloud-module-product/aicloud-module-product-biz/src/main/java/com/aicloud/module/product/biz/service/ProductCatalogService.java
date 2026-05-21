package com.aicloud.module.product.biz.service;

import com.aicloud.module.product.biz.entity.AiProductCategory;
import com.aicloud.module.product.biz.entity.AiProductSpu;
import com.aicloud.module.product.biz.mapper.ProductCategoryMapper;
import com.aicloud.module.product.biz.mapper.ProductSpuMapper;
import com.aicloud.module.product.biz.model.CategorySaveRequest;
import com.aicloud.module.product.biz.model.PageResponse;
import com.aicloud.module.product.biz.model.ProductSaveRequest;
import com.aicloud.module.product.biz.model.ProductStatusUpdateRequest;
import com.aicloud.module.product.biz.model.ProductStockUpdateRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductCatalogService {

    private final ProductSpuMapper productSpuMapper;
    private final ProductCategoryMapper productCategoryMapper;

    public ProductCatalogService(ProductSpuMapper productSpuMapper, ProductCategoryMapper productCategoryMapper) {
        this.productSpuMapper = productSpuMapper;
        this.productCategoryMapper = productCategoryMapper;
    }

    public PageResponse<AiProductSpu> listSpu(Long tenantId, String keyword, Long categoryId, Integer status, long pageNo, long pageSize) {
        Page<AiProductSpu> page = new Page<>(Math.max(pageNo, 1), Math.min(Math.max(pageSize, 1), 100));
        LambdaQueryWrapper<AiProductSpu> wrapper = new LambdaQueryWrapper<AiProductSpu>()
                .eq(AiProductSpu::getTenantId, tenantId)
                .eq(categoryId != null, AiProductSpu::getCategoryId, categoryId)
                .eq(status != null, AiProductSpu::getStatus, status)
                .and(org.springframework.util.StringUtils.hasText(keyword), query -> query.like(AiProductSpu::getName, keyword).or().like(AiProductSpu::getSpuNo, keyword))
                .orderByDesc(AiProductSpu::getSort)
                .orderByDesc(AiProductSpu::getId);
        Page<AiProductSpu> result = productSpuMapper.selectPage(page, wrapper);
        PageResponse<AiProductSpu> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setPageNo(result.getCurrent());
        response.setPageSize(result.getSize());
        response.setList(result.getRecords());
        return response;
    }

    public AiProductSpu getSpu(Long id) {
        AiProductSpu spu = productSpuMapper.selectById(id);
        if (spu == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        return spu;
    }

    public AiProductSpu saveSpu(ProductSaveRequest request) {
        AiProductSpu spu = request.getId() == null ? new AiProductSpu() : getSpu(request.getId());
        spu.setTenantId(request.getTenantId());
        spu.setSpuNo(StringUtils.hasText(request.getSpuNo()) ? request.getSpuNo() : defaultSpuNo());
        spu.setName(request.getName());
        spu.setSubTitle(request.getSubTitle());
        spu.setCategoryId(request.getCategoryId());
        spu.setBrandName(request.getBrandName());
        spu.setUnitName(request.getUnitName());
        spu.setCoverUrl(request.getCoverUrl());
        spu.setSort(request.getSort() == null ? 0 : request.getSort());
        spu.setStatus(request.getStatus());
        spu.setSalePrice(request.getSalePrice());
        spu.setStock(request.getStock());
        spu.setUpdateTime(LocalDateTime.now());
        if (spu.getId() == null) {
            spu.setCreateTime(LocalDateTime.now());
            productSpuMapper.insert(spu);
        } else {
            productSpuMapper.updateById(spu);
        }
        return spu;
    }

    public AiProductSpu updateStock(ProductStockUpdateRequest request) {
        AiProductSpu spu = getSpu(request.getId());
        spu.setStock(request.getStock());
        spu.setUpdateTime(LocalDateTime.now());
        productSpuMapper.updateById(spu);
        return spu;
    }

    public AiProductSpu updateStatus(ProductStatusUpdateRequest request) {
        AiProductSpu spu = getSpu(request.getId());
        spu.setStatus(request.getStatus());
        spu.setUpdateTime(LocalDateTime.now());
        productSpuMapper.updateById(spu);
        return spu;
    }

    public List<AiProductCategory> categoryTree(Long tenantId) {
        return productCategoryMapper.selectList(new LambdaQueryWrapper<AiProductCategory>()
                .eq(AiProductCategory::getTenantId, tenantId)
                .orderByAsc(AiProductCategory::getSort)
                .orderByAsc(AiProductCategory::getId));
    }

    public AiProductCategory saveCategory(CategorySaveRequest request) {
        AiProductCategory category = request.getId() == null ? new AiProductCategory() : getCategory(request.getId());
        category.setTenantId(request.getTenantId());
        category.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        category.setName(request.getName());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setStatus(request.getStatus());
        category.setUpdateTime(LocalDateTime.now());
        if (category.getId() == null) {
            category.setCreateTime(LocalDateTime.now());
            productCategoryMapper.insert(category);
        } else {
            productCategoryMapper.updateById(category);
        }
        return category;
    }

    public void deleteCategory(Long id) {
        long childCount = productCategoryMapper.selectCount(new LambdaQueryWrapper<AiProductCategory>().eq(AiProductCategory::getParentId, id));
        if (childCount > 0) { throw new IllegalArgumentException("存在子分类，不能删除"); }
        productCategoryMapper.deleteById(id);
    }

    public AiProductSpu changeStock(Long id, Integer delta) {
        AiProductSpu spu = getSpu(id);
        int after = (spu.getStock() == null ? 0 : spu.getStock()) + delta;
        if (after < 0) { throw new IllegalArgumentException("库存不足"); }
        spu.setStock(after);
        spu.setUpdateTime(LocalDateTime.now());
        productSpuMapper.updateById(spu);
        return spu;
    }

    public void deleteSpu(Long id) {
        getSpu(id);
        productSpuMapper.deleteById(id);
    }

    public AiProductCategory updateCategoryStatus(Long id, Integer status) {
        AiProductCategory category = getCategory(id);
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        productCategoryMapper.updateById(category);
        return category;
    }

    public AiProductCategory getCategory(Long id) {
        AiProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new IllegalArgumentException("商品分类不存在");
        }
        return category;
    }

    private String defaultSpuNo() {
        return "SPU-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
