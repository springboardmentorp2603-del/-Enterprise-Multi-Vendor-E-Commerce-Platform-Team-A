package com.shopstack.modules.product.repository;

import com.shopstack.modules.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByCategoryName(String categoryName);
    boolean existsByCategoryName(String categoryName);
}