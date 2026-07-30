package com.shopstack.modules.product.mapper;

import com.shopstack.common.config.MapStructConfig;
import com.shopstack.modules.product.dto.requests.CreateCategoryRequest;
import com.shopstack.modules.product.dto.requests.UpdateCategoryRequest;
import com.shopstack.modules.product.dto.responses.CategoryResponse;
import com.shopstack.modules.product.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface CategoryMapper {

    Category toEntity(CreateCategoryRequest request);

    CategoryResponse toResponse(Category category);

    void updateEntity(UpdateCategoryRequest request,
                      @MappingTarget Category category);
}