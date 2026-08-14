package com.shopstack.modules.warehouse.dto.responses;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseResponse {
    private UUID id;
    private String code;
    private String name;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Integer capacity;
    private Integer occupancy;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
