package com.shopstack.modules.product.controller;

import com.shopstack.modules.product.dto.CategoryCommissionRateDto;
import com.shopstack.modules.product.entity.Category;
import com.shopstack.modules.product.entity.CategoryCommissionRate;
import com.shopstack.modules.product.repository.CategoryCommissionRateRepository;
import com.shopstack.modules.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/categories/commission-rates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryCommissionRateController {

    private final CategoryCommissionRateRepository categoryCommissionRateRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<CategoryCommissionRateDto>> listRates() {
        List<CategoryCommissionRate> rates = categoryCommissionRateRepository.findAll();
        List<CategoryCommissionRateDto> dtos = new ArrayList<>();
        
        for (CategoryCommissionRate rate : rates) {
            String catName = "Unknown";
            Optional<Category> catOpt = categoryRepository.findById(rate.getCategoryId());
            if (catOpt.isPresent()) {
                catName = catOpt.get().getCategoryName();
            }
            
            dtos.add(CategoryCommissionRateDto.builder()
                    .id(rate.getId())
                    .categoryId(rate.getCategoryId())
                    .categoryName(catName)
                    .commissionRate(rate.getCommissionRate())
                    .active(rate.getActive())
                    .effectiveFrom(rate.getEffectiveFrom())
                    .effectiveTo(rate.getEffectiveTo())
                    .build());
        }
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<?> createRate(@RequestBody CategoryCommissionRateDto dto) {
        if (dto.getCategoryId() == null) {
            return ResponseEntity.badRequest().body("Category ID is required");
        }
        if (dto.getCommissionRate() == null || dto.getCommissionRate().compareTo(BigDecimal.ZERO) < 0 || dto.getCommissionRate().compareTo(BigDecimal.valueOf(100)) > 0) {
            return ResponseEntity.badRequest().body("Commission rate must be between 0 and 100");
        }

        LocalDateTime from = dto.getEffectiveFrom() != null ? dto.getEffectiveFrom() : LocalDateTime.now();

        // Expire / deactivate previous active rates for the same category to maintain single active rate constraint
        List<CategoryCommissionRate> existing = categoryCommissionRateRepository.findByCategoryId(dto.getCategoryId());
        for (CategoryCommissionRate rate : existing) {
            if (Boolean.TRUE.equals(rate.getActive())) {
                rate.setActive(false);
                if (rate.getEffectiveTo() == null || rate.getEffectiveTo().isAfter(from)) {
                    rate.setEffectiveTo(from);
                }
                categoryCommissionRateRepository.save(rate);
            }
        }

        CategoryCommissionRate rate = CategoryCommissionRate.builder()
                .categoryId(dto.getCategoryId())
                .commissionRate(dto.getCommissionRate())
                .active(true)
                .effectiveFrom(from)
                .effectiveTo(dto.getEffectiveTo())
                .build();

        CategoryCommissionRate saved = categoryCommissionRateRepository.save(rate);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRate(@PathVariable UUID id, @RequestBody CategoryCommissionRateDto dto) {
        CategoryCommissionRate rate = categoryCommissionRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission rate not found: " + id));

        if (dto.getCommissionRate() == null || dto.getCommissionRate().compareTo(BigDecimal.ZERO) < 0 || dto.getCommissionRate().compareTo(BigDecimal.valueOf(100)) > 0) {
            return ResponseEntity.badRequest().body("Commission rate must be between 0 and 100");
        }

        // To ensure backward compatibility/history preservation, we don't overwrite if it's already active/effective
        // Instead, we update the status or expiration
        rate.setCommissionRate(dto.getCommissionRate());
        if (dto.getEffectiveTo() != null) {
            rate.setEffectiveTo(dto.getEffectiveTo());
        }
        if (dto.getActive() != null) {
            rate.setActive(dto.getActive());
        }

        CategoryCommissionRate saved = categoryCommissionRateRepository.save(rate);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CategoryCommissionRate> toggleStatus(@PathVariable UUID id) {
        CategoryCommissionRate rate = categoryCommissionRateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission rate not found: " + id));

        rate.setActive(!Boolean.TRUE.equals(rate.getActive()));
        if (!rate.getActive()) {
            rate.setEffectiveTo(LocalDateTime.now());
        } else {
            rate.setEffectiveTo(null);
        }

        CategoryCommissionRate saved = categoryCommissionRateRepository.save(rate);
        return ResponseEntity.ok(saved);
    }
}
