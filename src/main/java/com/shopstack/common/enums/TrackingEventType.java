package com.shopstack.common.enums;

public enum TrackingEventType {
    LABEL_CREATED("Shipping label created"),
    PICKED_UP("Package picked up"),
    IN_TRANSIT("In transit"),
    ARRIVAL_SCAN("Arrival scan at facility"),
    DEPARTURE_SCAN("Departure scan from facility"),
    OUT_FOR_DELIVERY("Out for delivery"),
    DELIVERED("Delivered"),
    DELIVERY_ATTEMPT("Delivery attempt made"),
    EXCEPTION("Exception occurred");
    
    private String description;
    
    TrackingEventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}