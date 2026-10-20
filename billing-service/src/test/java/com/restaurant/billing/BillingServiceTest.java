package com.restaurant.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.restaurant.billing.dto.BillRequest;
import com.restaurant.billing.dto.BillResponse;
import com.restaurant.billing.service.BillingService;

/** Plain unit test - no Spring needed because BillingService gets its values through the constructor. */
class BillingServiceTest {

    private final BillingService billingService = new BillingService(5.0, 10.0, 1000.0, 10.0);

    @Test
    void smallBillHasNoDiscount() {
        BillResponse bill = billingService.calculate(new BillRequest(1L, "Test", 500.0));
        assertEquals(25.0, bill.gst());
        assertEquals(50.0, bill.serviceCharge());
        assertEquals(0.0, bill.discount());
        assertEquals(575.0, bill.total());
    }

    @Test
    void bigBillGetsDiscount() {
        BillResponse bill = billingService.calculate(new BillRequest(2L, "Test", 1000.0));
        assertEquals(100.0, bill.discount());
        assertEquals(1050.0, bill.total()); // 1000 + 50 + 100 - 100
    }
}
