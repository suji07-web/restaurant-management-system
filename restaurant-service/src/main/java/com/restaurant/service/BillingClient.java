package com.restaurant.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.restaurant.dto.BillRequest;
import com.restaurant.dto.BillResponse;
import com.restaurant.exception.BillingServiceException;
import com.restaurant.model.CustomerOrder;

/**
 * MICROSERVICE COMMUNICATION:
 * Calls the separate billing-service (port 8082) over HTTP/REST.
 *   POST http://localhost:8082/api/bills/calculate   body: {"orderId":1,"customerName":"..","subtotal":540.0}
 */
@Component
public class BillingClient {

    private final RestClient billingRestClient;

    public BillingClient(RestClient billingRestClient) {
        this.billingRestClient = billingRestClient;
    }

    public BillResponse calculateBill(CustomerOrder order) {
        BillRequest request = new BillRequest(order.getId(), order.getCustomerName(), order.getSubtotal());
        try {
            return billingRestClient.post()
                    .uri("/api/bills/calculate")
                    .body(request)
                    .retrieve()
                    .body(BillResponse.class);
        } catch (RestClientException ex) {
            throw new BillingServiceException(
                    "Billing service is not reachable. Start billing-service on port 8082 and try again.", ex);
        }
    }
}
