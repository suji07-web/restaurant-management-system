package com.restaurant.dto;

/** The bill returned by the billing-service (same JSON shape as in that service). */
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
