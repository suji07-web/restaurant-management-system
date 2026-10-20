package com.restaurant.model;

/** Life cycle of an order. Stored as text in the database (@Enumerated(EnumType.STRING)). */
public enum OrderStatus {
    PLACED,
    PREPARING,
    SERVED,
    PAID,
    CANCELLED
}
