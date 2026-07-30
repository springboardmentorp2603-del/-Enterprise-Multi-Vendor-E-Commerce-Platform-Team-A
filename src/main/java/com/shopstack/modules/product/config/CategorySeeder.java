package com.shopstack.modules.product.config;

import com.shopstack.modules.product.entity.Category;
import com.shopstack.modules.product.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
public class CategorySeeder implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(CategorySeeder.class);

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {

        if (categoryRepository.count() > 0) {
            return;
        }

        List<Category> categories = List.of(

                Category.builder()
                        .categoryName("Electronics")
                        .description("Mobiles, Laptops, Accessories")
                        .build(),

                Category.builder()
                        .categoryName("Fashion")
                        .description("Men & Women Clothing")
                        .build(),

                Category.builder()
                        .categoryName("Home & Kitchen")
                        .description("Furniture & Kitchen Appliances")
                        .build(),

                Category.builder()
                        .categoryName("Books")
                        .description("Educational and Story Books")
                        .build(),

                Category.builder()
                        .categoryName("Sports")
                        .description("Sports Equipment")
                        .build()

        );

        categoryRepository.saveAll(categories);

        log.info("Sample Categories Seeded Successfully");
    }
}