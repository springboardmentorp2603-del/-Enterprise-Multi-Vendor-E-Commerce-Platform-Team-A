package com.shopstack.modules.product.config;

import com.shopstack.modules.product.entity.Category;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.repository.CategoryRepository;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.vendor.entity.Vendor;
import com.shopstack.modules.vendor.repository.VendorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(5)
public class ProductSeeder implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(ProductSeeder.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VendorRepository vendorRepository;

    public ProductSeeder(ProductRepository productRepository,
                         CategoryRepository categoryRepository,
                         VendorRepository vendorRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.vendorRepository = vendorRepository;
    }

    @Override
    public void run(String... args) {

        if (productRepository.count() > 0) {
            return;
        }

        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) {
            log.warn("No categories found. Product seeding skipped.");
            return;
        }

        List<Vendor> vendors = vendorRepository.findAll();

        if (vendors.isEmpty()) {
            log.warn("No vendors found. Product seeding skipped.");
            return;
        }

        Category electronics =
                categories.stream()
                        .filter(c -> c.getCategoryName().equalsIgnoreCase("Electronics"))
                        .findFirst()
                        .orElse(categories.get(0));

        Long vendor = vendors.get(0).getVendorId();

        Product laptop = Product.builder()
                .productName("MacBook Air M3")
                .brand("Apple")
                .description("Apple MacBook Air with M3 Chip")
                .price(new BigDecimal("119999"))
                .stockQuantity(15)
                .imageUrl("https://images.unsplash.com/photo-1517336714739-489689fd1ca8")
                .featured(true)
                .category(electronics)
                .vendorId(vendor)
                .active(true)
                .build();

        Product earbuds = Product.builder()
                .productName("Galaxy Buds Pro")
                .brand("Samsung")
                .description("Wireless ANC Earbuds")
                .price(new BigDecimal("9999"))
                .stockQuantity(40)
                .imageUrl("https://images.unsplash.com/photo-1583394838336-acd977736f90")
                .featured(true)
                .category(electronics)
                .vendorId(vendor)
                .active(true)
                .build();

        Product keyboard = Product.builder()
                .productName("Mechanical Keyboard")
                .brand("Logitech")
                .description("RGB Mechanical Gaming Keyboard")
                .price(new BigDecimal("4999"))
                .stockQuantity(30)
                .imageUrl("https://images.unsplash.com/photo-1511467687858-23d96c32e4ae")
                .featured(false)
                .category(electronics)
                .vendorId(vendor)
                .active(true)
                .build();

        productRepository.saveAll(
                List.of(
                        laptop,
                        earbuds,
                        keyboard
                )
        );

        log.info("Sample products inserted successfully.");
    }
}