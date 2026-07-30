package com.shopstack.modules.pricing.mapper;

import com.shopstack.common.config.MapStructConfig;
import com.shopstack.modules.pricing.dto.responses.DiscountResponse;
import com.shopstack.modules.pricing.dto.responses.PriceHistoryResponse;
import com.shopstack.modules.pricing.entity.Discount;
import com.shopstack.modules.pricing.entity.PriceHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface PricingMapper {

    // PriceHistory's fields are already flat IDs (productId, vendorId,
    // changedBy) rather than relations, so no explicit @Mapping is needed.
    PriceHistoryResponse toResponse(PriceHistory priceHistory);

    List<PriceHistoryResponse> toPriceHistoryResponseList(List<PriceHistory> history);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "category.id", target = "categoryId")
    // "currentlyActive" maps automatically from Discount#isCurrentlyActive()
    // by MapStruct's bean-property naming convention, same as
    // InventoryResponse.lowStock <- Inventory#isLowStock().
    DiscountResponse toResponse(Discount discount);

    List<DiscountResponse> toDiscountResponseList(List<Discount> discounts);
}