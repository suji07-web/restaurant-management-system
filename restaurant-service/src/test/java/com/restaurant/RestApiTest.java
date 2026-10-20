package com.restaurant;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.dto.BillResponse;
import com.restaurant.service.BillingClient;

/** Calls the REST endpoints the same way Postman does (GET / POST with JSON). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class RestApiTest {

    @Autowired
    private MockMvc mockMvc;

    // the real billing-service is not running during tests, so we replace the client with a mock
    @MockitoBean
    private BillingClient billingClient;

    @Test
    void getMenuReturnsJsonWithType() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").exists());
    }

    @Test
    void postFoodItem() throws Exception {
        String json = """
                {"type": "FOOD", "name": "Masala Dosa", "price": 120, "category": "Main Course",
                 "vegetarian": true, "spiceLevel": "MILD"}
                """;
        mockMvc.perform(post("/api/menu").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.type").value("FOOD"));
    }

    @Test
    void postBeverageWithOnlyRequiredFields() throws Exception {
        String json = """
                {"type": "BEVERAGE", "name": "Buttermilk", "price": 40, "category": "Drinks"}
                """;
        mockMvc.perform(post("/api/menu").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("BEVERAGE"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void postInvalidMenuItemReturns400() throws Exception {
        String json = """
                {"type": "BEVERAGE", "name": "", "price": -5, "category": "Drinks"}
                """;
        mockMvc.perform(post("/api/menu").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("price")));
    }

    @Test
    void postOrderCalculatesSubtotal() throws Exception {
        String json = """
                {"customerName": "Test", "tableNumber": 2,
                 "items": [{"menuItemId": 1, "quantity": 2}]}
                """;
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.subtotal").value(440.0)); // Paneer Tikka 220 x 2
    }

    @Test
    void unknownOrderReturns404() throws Exception {
        mockMvc.perform(get("/api/orders/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void billComesFromBillingService() throws Exception {
        when(billingClient.calculateBill(any()))
                .thenReturn(new BillResponse(1L, "Arjun", 100, 5, 10, 0, 115, "now"));

        mockMvc.perform(get("/api/orders/1/bill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(115.0));
    }
}
