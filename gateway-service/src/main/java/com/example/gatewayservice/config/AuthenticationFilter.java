package com.example.gatewayservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtProvider jwtProvider;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private final List<String> openApiEndpoints = List.of(
            "api/auth/login",
            "api/auth/register",
            "api/auth/refresh"
    );


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (openApiEndpoints.stream().anyMatch(path -> request.getURI().getPath().contains(path))) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String accessToken = authHeader.substring(7);

        try {
            Claims claims = jwtProvider.extractAllClaims(accessToken);
            String type = claims.get("type", String.class);
            if (!"access".equals(type) || !jwtProvider.validateToken(accessToken)){
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            return redisTemplate.hasKey("blacklist:"+accessToken)
                    .flatMap(isBlacklisted -> {
                        if (Boolean.TRUE.equals(isBlacklisted)){
                            return onError(exchange, HttpStatus.UNAUTHORIZED);
                        }

                        String username = claims.getSubject();
                        List<String> roles = claims.get("roles", List.class);
                        List<String> permissions = claims.get("permissions", List.class);

                        String rolesJson = "[]";
                        if (roles != null){
                            try {
                                rolesJson = objectMapper.writeValueAsString(roles);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        String permissionsJson = "[]";
                        if (permissions != null){
                            try {
                                permissionsJson = objectMapper.writeValueAsString(permissions);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        ServerHttpRequest mutedRequest = exchange.getRequest().mutate()
                                .header("X-Username", username)
                                .header("X-Roles", rolesJson)
                                .header("X-Permissions", permissionsJson)
                                .build();
                        ServerWebExchange mutedExchange = exchange.mutate().request(mutedRequest).build();
                        return chain.filter(mutedExchange);
                    });

        }catch (Exception e){
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus){
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
