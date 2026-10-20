package com.restaurant.dto;

/** What we send to the billing-service. */
public record BillRequest(Long orderId, String customerName, double subtotal) {
}
