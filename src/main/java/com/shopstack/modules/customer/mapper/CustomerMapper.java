package com.shopstack.modules.customer.mapper;

import com.shopstack.modules.customer.dto.responses.CustomerAddressResponse;
import com.shopstack.modules.customer.dto.responses.CustomerResponse;
import com.shopstack.modules.customer.entity.Customer;
import com.shopstack.modules.customer.entity.CustomerAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(source = "user.id", target = "userId")
    CustomerResponse toResponse(Customer customer);

    CustomerAddressResponse toAddressResponse(CustomerAddress address);

    List<CustomerAddressResponse> toAddressResponseList(List<CustomerAddress> addresses);

    default Instant map(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
