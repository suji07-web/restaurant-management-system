package com.restaurant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Java-based Spring configuration. Every @Bean method creates an object that
 * Spring manages and can inject into other classes (here: into BillingClient).
 */
@Configuration
public class AppConfig {

    @Bean
    public RestClient billingRestClient(@Value("${billing.service.url}") String billingServiceUrl) {
        return RestClient.builder()
                .baseUrl(billingServiceUrl)
                .build();
    }
}
