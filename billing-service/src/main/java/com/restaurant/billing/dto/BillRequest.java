package com.restaurant.billing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Sent by restaurant-service: {"orderId": 1, "customerName": "Arjun", "subtotal": 900.0} */
public record BillRequest(
        @NotNull(message = "orderId is required") Long orderId,
        String customerName,
        @PositiveOrZero(message = "subtotal cannot be negative") double subtotal) {
}
