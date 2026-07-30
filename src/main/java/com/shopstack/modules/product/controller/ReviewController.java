package com.shopstack.modules.product.controller;

import com.shopstack.modules.product.dto.requests.CreateReviewRequest;
import com.shopstack.modules.product.dto.responses.ReviewResponse;
import com.shopstack.modules.product.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{productId}")
    public ReviewResponse addReview(
            @PathVariable UUID productId,
            @Valid @RequestBody CreateReviewRequest request) {

        return reviewService.addReview(productId, request);
    }

    @GetMapping("/{productId}")
    public List<ReviewResponse> getReviews(
            @PathVariable UUID productId) {

        return reviewService.getReviewsByProduct(productId);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(
            @PathVariable UUID reviewId) {

        reviewService.deleteReview(reviewId);
    }
}