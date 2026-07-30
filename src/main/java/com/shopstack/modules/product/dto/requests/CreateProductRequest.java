package com.shopstack.modules.product.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank
    @Size(max = 150)
    private String productName;

    @Size(max = 100)
    private String brand;

    private String description;

    private String imageUrl;

    private String features;

    private MultipartFile image;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer stockQuantity;

    private BigDecimal discountPercentage;

    // Not required from the client: for a VENDOR caller this is derived from their
    // logged-in vendor profile. ADMIN callers may supply it explicitly.
    private Long vendorId;

    @NotNull
    private UUID categoryId;

    private Boolean featured;
}