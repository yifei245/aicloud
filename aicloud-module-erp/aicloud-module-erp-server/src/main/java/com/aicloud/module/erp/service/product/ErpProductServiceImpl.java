package com.aicloud.module.erp.service.product;

import cn.hutool.core.collection.CollUtil;
import com.aicloud.framework.common.enums.CommonStatusEnum;
import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.framework.common.util.collection.MapUtils;
import com.aicloud.framework.common.util.object.BeanUtils;
import com.aicloud.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import com.aicloud.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import com.aicloud.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;
import com.aicloud.module.erp.dal.dataobject.product.ErpProductCategoryDO;
import com.aicloud.module.erp.dal.dataobject.product.ErpProductDO;
import com.aicloud.module.erp.dal.dataobject.product.ErpProductUnitDO;
import com.aicloud.module.erp.dal.mysql.product.ErpProductMapper;
import com.aicloud.module.erp.service.purchase.ErpPurchaseInService;
import com.aicloud.module.erp.service.purchase.ErpPurchaseOrderService;
import com.aicloud.module.erp.service.purchase.ErpPurchaseReturnService;
import com.aicloud.module.erp.service.sale.ErpSaleOrderService;
import com.aicloud.module.erp.service.sale.ErpSaleOutService;
import com.aicloud.module.erp.service.sale.ErpSaleReturnService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static com.aicloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.aicloud.framework.common.util.collection.CollectionUtils.*;
import static com.aicloud.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP 产品 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpProductServiceImpl implements ErpProductService {

    @Resource
    private ErpProductMapper productMapper;
    @Resource
    private ErpWmsIntegration wmsIntegration;

    @Resource
    private ErpProductCategoryService productCategoryService;
    @Resource
    private ErpProductUnitService productUnitService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpPurchaseOrderService purchaseOrderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpPurchaseInService purchaseInService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpPurchaseReturnService purchaseReturnService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpSaleOrderService saleOrderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpSaleOutService saleOutService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private ErpSaleReturnService saleReturnService;

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Long createProduct(ProductSaveReqVO createReqVO) {
        // TODO 芋艿：校验分类
        // 插入
        ErpProductDO product = BeanUtils.toBean(createReqVO, ErpProductDO.class);
        wmsIntegration.validateMaster(product);
        productMapper.insert(product);
        wmsIntegration.changed(product);
        // 返回
        return product.getId();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductSaveReqVO updateReqVO) {
        // TODO 芋艿：校验分类
        // 校验存在
        validateProductExists(updateReqVO.getId());
        if (!java.util.Objects.equals(productMapper.selectById(updateReqVO.getId()).getUnitId(), updateReqVO.getUnitId())) {
            throw new IllegalArgumentException("商品基本计量单位不能直接更换；请新建商品并通过明确的转换单据处理库存");
        }
        // 更新
        ErpProductDO updateObj = BeanUtils.toBean(updateReqVO, ErpProductDO.class);
        wmsIntegration.validateMaster(updateObj);
        productMapper.updateById(updateObj);
        wmsIntegration.changed(productMapper.selectById(updateReqVO.getId()));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        // 校验存在
        validateProductExists(id);
        // 校验未被任何业务单据明细引用，避免删除后产生悬空引用
        if (purchaseOrderService.getPurchaseOrderItemCountByProductId(id) > 0) {
            throw exception(PRODUCT_DELETE_FAIL_PURCHASE_ORDER_EXISTS);
        }
        if (purchaseInService.getPurchaseInItemCountByProductId(id) > 0) {
            throw exception(PRODUCT_DELETE_FAIL_PURCHASE_IN_EXISTS);
        }
        if (purchaseReturnService.getPurchaseReturnItemCountByProductId(id) > 0) {
            throw exception(PRODUCT_DELETE_FAIL_PURCHASE_RETURN_EXISTS);
        }
        if (saleOrderService.getSaleOrderItemCountByProductId(id) > 0) {
            throw exception(PRODUCT_DELETE_FAIL_SALE_ORDER_EXISTS);
        }
        if (saleOutService.getSaleOutItemCountByProductId(id) > 0) {
            throw exception(PRODUCT_DELETE_FAIL_SALE_OUT_EXISTS);
        }
        if (saleReturnService.getSaleReturnItemCountByProductId(id) > 0) {
            throw exception(PRODUCT_DELETE_FAIL_SALE_RETURN_EXISTS);
        }
        // 删除
        ErpProductDO product = productMapper.selectById(id);
        productMapper.deleteById(id);
        product.setStatus(CommonStatusEnum.DISABLE.getStatus());
        wmsIntegration.changed(product);
    }

    @Override
    public List<ErpProductDO> validProductList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = productMapper.selectByIds(ids);
        Map<Long, ErpProductDO> productMap = convertMap(list, ErpProductDO::getId);
        for (Long id : ids) {
            ErpProductDO product = productMap.get(id);
            if (productMap.get(id) == null) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            if (CommonStatusEnum.isDisable(product.getStatus())) {
                throw exception(PRODUCT_NOT_ENABLE, product.getName());
            }
        }
        return list;
    }

    private void validateProductExists(Long id) {
        if (productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    @Override
    public ErpProductDO getProduct(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public List<ErpProductRespVO> getProductVOListByStatus(Integer status) {
        List<ErpProductDO> list = productMapper.selectListByStatus(status);
        return buildProductVOList(list);
    }

    @Override
    public List<ErpProductRespVO> getProductVOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDO> list = productMapper.selectByIds(ids);
        return buildProductVOList(list);
    }

    @Override
    public PageResult<ErpProductRespVO> getProductVOPage(ErpProductPageReqVO pageReqVO) {
        PageResult<ErpProductDO> pageResult = productMapper.selectPage(pageReqVO);
        return new PageResult<>(buildProductVOList(pageResult.getList()), pageResult.getTotal());
    }

    private List<ErpProductRespVO> buildProductVOList(List<ErpProductDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductCategoryDO> categoryMap = productCategoryService.getProductCategoryMap(
                convertSet(list, ErpProductDO::getCategoryId));
        Map<Long, ErpProductUnitDO> unitMap = productUnitService.getProductUnitMap(
                convertSet(list, ErpProductDO::getUnitId));
        return BeanUtils.toBean(list, ErpProductRespVO.class, product -> {
            MapUtils.findAndThen(categoryMap, product.getCategoryId(),
                    category -> product.setCategoryName(category.getName()));
            MapUtils.findAndThen(unitMap, product.getUnitId(),
                    unit -> product.setUnitName(unit.getName()));
        });
    }

    @Override
    public Long getProductCountByCategoryId(Long categoryId) {
        return productMapper.selectCountByCategoryId(categoryId);
    }

    @Override
    public Long getProductCountByUnitId(Long unitId) {
        return productMapper.selectCountByUnitId(unitId);
    }

}
