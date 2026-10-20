package com.restaurant.billing.dto;

public record BillResponse(
        Long orderId,
        String customerName,
        double subtotal,
        double gst,
        double serviceCharge,
        double discount,
        double total,
        String generatedAt) {
}
