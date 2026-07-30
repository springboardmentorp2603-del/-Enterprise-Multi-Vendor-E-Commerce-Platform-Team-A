package com.shopstack.modules.product.service;

import com.shopstack.modules.product.dto.requests.CreateCategoryRequest;
import com.shopstack.modules.product.dto.requests.UpdateCategoryRequest;
import com.shopstack.modules.product.dto.responses.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request);

    void deleteCategory(UUID id);

    CategoryResponse getCategoryById(UUID id);

    List<CategoryResponse> getAllCategories();
}