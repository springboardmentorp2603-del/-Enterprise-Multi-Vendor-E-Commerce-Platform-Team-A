package com.shopstack.modules.customer.dto.responses;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private boolean active;
    private List<CustomerAddressResponse> addresses;
    private Instant createdAt;
    private Instant updatedAt;
}
