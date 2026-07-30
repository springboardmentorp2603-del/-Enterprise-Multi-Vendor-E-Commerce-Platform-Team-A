package com.shopstack.modules.product.service;

import com.shopstack.common.exception.BadRequestException;
import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.product.dto.requests.CreateCategoryRequest;
import com.shopstack.modules.product.dto.requests.UpdateCategoryRequest;
import com.shopstack.modules.product.dto.responses.CategoryResponse;
import com.shopstack.modules.product.entity.Category;
import com.shopstack.modules.product.mapper.CategoryMapper;
import com.shopstack.modules.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            throw new BadRequestException("Category already exists.");
        }

        Category category = categoryMapper.toEntity(request);

        return categoryMapper.toResponse(
                categoryRepository.save(category)
        );
    }

    @Override
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        categoryMapper.updateEntity(request, category);

        return categoryMapper.toResponse(
                categoryRepository.save(category)
        );
    }

    @Override
    public void deleteCategory(UUID id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found."));

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}