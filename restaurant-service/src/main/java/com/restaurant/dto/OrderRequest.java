package com.restaurant.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * JSON body for POST /api/orders
 * {
 *   "customerName": "Ravi", "tableNumber": 4,
 *   "items": [ {"menuItemId": 1, "quantity": 2}, {"menuItemId": 7, "quantity": 1} ]
 * }
 */
public record OrderRequest(
        @NotBlank(message = "Customer name is required") String customerName,
        @Min(value = 1, message = "Table number must be 1 or more") int tableNumber,
        @NotEmpty(message = "An order needs at least one item") List<@Valid Line> items) {

    public record Line(
            @NotNull(message = "menuItemId is required") Long menuItemId,
            @Min(value = 1, message = "Quantity must be 1 or more") int quantity) {
    }
}
