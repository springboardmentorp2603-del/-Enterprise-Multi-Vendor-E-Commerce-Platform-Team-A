package com.shopstack.modules.order.entity;

import com.shopstack.common.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String orderId;
    
    @Column(unique = true)
    private String trackingNumber;
    
    private String carrier;
    private String carrierService;
    
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;
    
    private LocalDateTime labelCreatedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime inTransitAt;
    private LocalDateTime outForDeliveryAt;
    private LocalDateTime deliveredAt;
    
    private String estimatedDeliveryDate;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shipment")
    private List<TrackingEvent> trackingEvents;
    
    private String fromAddress;
    private String toAddress;
    private Double weight;
    private String weightUnit;
    private Double shippingCost;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}