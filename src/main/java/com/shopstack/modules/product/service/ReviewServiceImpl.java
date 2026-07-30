package com.shopstack.modules.product.service;

import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.product.dto.requests.CreateReviewRequest;
import com.shopstack.modules.product.dto.responses.ReviewResponse;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.entity.Review;
import com.shopstack.modules.product.mapper.ReviewMapper;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.product.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public ReviewResponse addReview(UUID productId,
                                    CreateReviewRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found."));

        Review review = reviewMapper.toEntity(request);

        review.setProduct(product);

        return reviewMapper.toResponse(
                reviewRepository.save(review)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(UUID productId) {

        return reviewRepository.findByProduct_Id(productId)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteReview(UUID reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Review not found."));

        reviewRepository.delete(review);
    }
}