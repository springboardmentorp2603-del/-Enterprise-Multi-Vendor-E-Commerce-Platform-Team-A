package com.shopstack.modules.product.service;

import com.shopstack.modules.product.dto.requests.CreateReviewRequest;
import com.shopstack.modules.product.dto.responses.ReviewResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    ReviewResponse addReview(UUID productId,
                             CreateReviewRequest request);

    List<ReviewResponse> getReviewsByProduct(UUID productId);

    void deleteReview(UUID reviewId);
}