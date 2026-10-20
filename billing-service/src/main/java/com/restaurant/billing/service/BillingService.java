package com.restaurant.billing.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.restaurant.billing.dto.BillRequest;
import com.restaurant.billing.dto.BillResponse;

/**
 * Calculates bills. This service has NO database: it only needs the order subtotal,
 * which shows that a microservice can be small and focused on ONE job.
 *
 *   gst            = subtotal * gst%
 *   service charge = subtotal * service%
 *   discount       = subtotal * discount%   (only when subtotal >= threshold)
 *   total          = subtotal + gst + service charge - discount
 */
@Service
public class BillingService {

    private final double gstPercent;
    private final double serviceChargePercent;
    private final double discountThreshold;
    private final double discountPercent;

    // values come from application.properties (billing.*)
    public BillingService(@Value("${billing.gst-percent}") double gstPercent,
                          @Value("${billing.service-charge-percent}") double serviceChargePercent,
                          @Value("${billing.discount-threshold}") double discountThreshold,
                          @Value("${billing.discount-percent}") double discountPercent) {
        this.gstPercent = gstPercent;
        this.serviceChargePercent = serviceChargePercent;
        this.discountThreshold = discountThreshold;
        this.discountPercent = discountPercent;
    }

    public BillResponse calculate(BillRequest request) {
        double subtotal = request.subtotal();
        double gst = round(subtotal * gstPercent / 100);
        double serviceCharge = round(subtotal * serviceChargePercent / 100);
        double discount = subtotal >= discountThreshold ? round(subtotal * discountPercent / 100) : 0;
        double total = round(subtotal + gst + serviceCharge - discount);
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss"));
        return new BillResponse(request.orderId(), request.customerName(), subtotal, gst, serviceCharge,
                discount, total, time);
    }

    public Map<String, Double> rates() {
        Map<String, Double> rates = new LinkedHashMap<>();
        rates.put("gstPercent", gstPercent);
        rates.put("serviceChargePercent", serviceChargePercent);
        rates.put("discountThreshold", discountThreshold);
        rates.put("discountPercent", discountPercent);
        return rates;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
