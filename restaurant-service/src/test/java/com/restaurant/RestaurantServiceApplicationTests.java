package com.restaurant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2") // tests use the in-memory H2 database, so no MySQL/PostgreSQL is needed
class RestaurantServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
