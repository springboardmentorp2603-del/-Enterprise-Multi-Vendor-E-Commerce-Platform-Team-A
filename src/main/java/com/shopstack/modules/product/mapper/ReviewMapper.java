package com.shopstack.modules.product.mapper;

import com.shopstack.common.config.MapStructConfig;
import com.shopstack.modules.product.dto.requests.CreateReviewRequest;
import com.shopstack.modules.product.dto.responses.ReviewResponse;
import com.shopstack.modules.product.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface ReviewMapper {

    @Mapping(target = "product", ignore = true)
    Review toEntity(CreateReviewRequest request);

    ReviewResponse toResponse(Review review);
}