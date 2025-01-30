package com.bankingproject.apigateway.Api_gateway.filter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.bankingproject.apigateway.Api_gateway.util.JwtUtil;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {
	
	@Autowired
    private  JwtUtil jwtUtil;

   

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest userInfo =null;

        final List<String> apiEndpoints = List.of("/auth/login", "/auth/register", "/eureka");

        Predicate<ServerHttpRequest> isApiSecured = r -> apiEndpoints.stream()
                .noneMatch(uri -> r.getURI().getPath().contains(uri));

        if (isApiSecured.test(request)) {
            if (authMissing(request)) return onError(exchange);

            String token = request.getHeaders().getOrEmpty("Authorization").get(0);

            if (token != null && token.startsWith("Bearer ")) token = token.substring(7);

            try {
                jwtUtil.validateToken(token);
                
                userInfo =  request
                .mutate()
                .header("loggedInUSer", jwtUtil.extractUserName(token))
                .build();
                
            } catch (Exception e) {
                return onError(exchange);
            }
        }
        return chain.filter(exchange.mutate().request(userInfo).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    private boolean authMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }
}