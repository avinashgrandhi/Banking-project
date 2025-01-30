package com.bankingproject.apigateway.Api_gateway.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bankingproject.apigateway.Api_gateway.filter.JwtAuthenticationFilter;

@Configuration
public class GatewayConfig {
	@Autowired
    private  JwtAuthenticationFilter filter;

   

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account-service", r -> r.path("/account/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://account-service"))

                .route("banking-security", r -> r.path("/auth/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://banking-security"))
                .build();
    }
}
