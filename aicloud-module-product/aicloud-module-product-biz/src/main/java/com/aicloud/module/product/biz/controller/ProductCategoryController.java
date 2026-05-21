package com.aicloud.module.product.biz.controller;

import com.aicloud.module.product.biz.entity.AiProductCategory;
import com.aicloud.module.product.biz.model.ApiResponse;
import com.aicloud.module.product.biz.model.CategorySaveRequest;
import com.aicloud.module.product.biz.service.ProductCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商品分类")
@RestController
public class ProductCategoryController {

    private final ProductCatalogService productCatalogService;

    public ProductCategoryController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @Operation(summary = "分类树")
    @GetMapping({"/product/category/tree", "/app/product/category/tree"})
    public ApiResponse<List<AiProductCategory>> tree(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(productCatalogService.categoryTree(tenantId));
    }

    @Operation(summary = "分类详情")
    @GetMapping("/product/category/{id}")
    public ApiResponse<AiProductCategory> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(productCatalogService.getCategory(id));
    }

    @Operation(summary = "创建或更新分类")
    @PostMapping({"/product/category/create", "/product/category/update"})
    public ApiResponse<AiProductCategory> save(@Valid @RequestBody CategorySaveRequest body) {
        return ApiResponse.ok(productCatalogService.saveCategory(body));
    }

    @Operation(summary = "分类启停")
    @PostMapping("/product/category/status")
    public ApiResponse<AiProductCategory> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        return ApiResponse.ok(productCatalogService.updateCategoryStatus(id, status));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/product/category/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Long id) {
        productCatalogService.deleteCategory(id);
        return ApiResponse.ok(true);
    }
}
