package com.shopstack.modules.order.entity;

import com.shopstack.common.enums.TrackingEventType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;
    
    private String trackingNumber;
    
    @Enumerated(EnumType.STRING)
    private TrackingEventType eventType;
    
    private String description;
    private String location;
    private LocalDateTime eventTime;
    private String carrierCode;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}