package com.restaurant.exception;

/** Thrown when the billing microservice cannot be reached. */
public class BillingServiceException extends RuntimeException {

    public BillingServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
