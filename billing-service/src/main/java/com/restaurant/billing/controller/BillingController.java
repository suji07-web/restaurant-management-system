package com.restaurant.billing.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.billing.dto.BillRequest;
import com.restaurant.billing.dto.BillResponse;
import com.restaurant.billing.service.BillingService;

import jakarta.validation.Valid;

/**
 * REST API of the billing microservice (port 8082).
 *
 *   GET  /api/bills/rates       current tax / charge / discount rates
 *   POST /api/bills/calculate   calculate a bill for an order (called by restaurant-service)
 */
@RestController
@RequestMapping("/api/bills")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/rates")
    public Map<String, Double> rates() {
        return billingService.rates();
    }

    @PostMapping("/calculate")
    public BillResponse calculate(@Valid @RequestBody BillRequest request) {
        return billingService.calculate(request);
    }
}
