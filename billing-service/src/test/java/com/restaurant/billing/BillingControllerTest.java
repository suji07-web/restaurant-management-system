package com.restaurant.billing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRates() throws Exception {
        mockMvc.perform(get("/api/bills/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gstPercent").value(5.0));
    }

    @Test
    void postCalculate() throws Exception {
        mockMvc.perform(post("/api/bills/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\": 7, \"customerName\": \"Asha\", \"subtotal\": 200.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(7))
                .andExpect(jsonPath("$.total").value(230.0));
    }

    @Test
    void postWithoutOrderIdIsRejected() throws Exception {
        mockMvc.perform(post("/api/bills/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subtotal\": 200.0}"))
                .andExpect(status().isBadRequest());
    }
}
