package com.aicloud.module.product.biz.controller;

import com.aicloud.module.product.biz.entity.AiProductSpu;
import com.aicloud.module.product.biz.model.ApiResponse;
import com.aicloud.module.product.biz.model.PageResponse;
import com.aicloud.module.product.biz.model.ProductSaveRequest;
import com.aicloud.module.product.biz.model.ProductStatusUpdateRequest;
import com.aicloud.module.product.biz.model.ProductStockUpdateRequest;
import com.aicloud.module.product.biz.service.ProductCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商品中心")
@RestController
@RequestMapping
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class ProductController {

    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @Operation(summary = "管理端商品分页")
    @GetMapping("/product/spu/list")
    public ApiResponse<PageResponse<AiProductSpu>> list(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") long pageSize) {
        return ApiResponse.ok(productCatalogService.listSpu(tenantId, keyword, categoryId, status, pageNo, pageSize));
    }

    @Operation(summary = "会员端商品列表")
    @GetMapping("/app/product/spu/list")
    public ApiResponse<PageResponse<AiProductSpu>> appList(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") long pageSize) {
        return ApiResponse.ok(productCatalogService.listSpu(tenantId, keyword, categoryId, 1, pageNo, pageSize));
    }

    @Operation(summary = "商品详情")
    @GetMapping({"/product/spu/{id}", "/app/product/spu/{id}"})
    public ApiResponse<AiProductSpu> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(productCatalogService.getSpu(id));
    }

    @Operation(summary = "创建或更新商品")
    @PostMapping({"/product/spu/create", "/product/spu/update"})
    public ApiResponse<AiProductSpu> save(@Valid @RequestBody ProductSaveRequest body) {
        return ApiResponse.ok(productCatalogService.saveSpu(body));
    }

    @Operation(summary = "更新商品状态")
    @PutMapping("/product/spu/status")
    public ApiResponse<AiProductSpu> updateStatus(@Valid @RequestBody ProductStatusUpdateRequest body) {
        return ApiResponse.ok(productCatalogService.updateStatus(body));
    }

    @Operation(summary = "更新库存")
    @PutMapping("/product/spu/stock")
    public ApiResponse<AiProductSpu> updateStock(@Valid @RequestBody ProductStockUpdateRequest body) {
        return ApiResponse.ok(productCatalogService.updateStock(body));
    }

    @Operation(summary = "增减库存")
    @PostMapping("/product/spu/stock/change")
    public ApiResponse<AiProductSpu> changeStock(@RequestParam Long id, @RequestParam Integer delta) {
        return ApiResponse.ok(productCatalogService.changeStock(id, delta));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/product/spu/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Long id) {
        productCatalogService.deleteSpu(id);
        return ApiResponse.ok(Boolean.TRUE);
    }
}
