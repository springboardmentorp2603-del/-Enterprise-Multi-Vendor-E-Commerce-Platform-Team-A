package com.shopstack.modules.product.mapper;

import com.shopstack.common.config.MapStructConfig;
import com.shopstack.modules.product.dto.requests.CreateProductRequest;
import com.shopstack.modules.product.dto.requests.UpdateProductRequest;
import com.shopstack.modules.product.dto.responses.ProductResponse;
import com.shopstack.modules.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    ProductResponse toResponse(Product product);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "vendorId", ignore = true)
    @Mapping(target = "approvalStatus", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    void updateEntity(UpdateProductRequest request,
                      @MappingTarget Product product);
}