package com.trung.orderservice.controller;

import com.trung.orderservice.dto.OrderCreateRequest;
import com.trung.orderservice.dto.OrderResponse;
import com.trung.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(@RequestHeader("X-UserId") Long userId,
                                                     @RequestHeader("X-Email") String userEmail,
                                                     @RequestBody OrderCreateRequest dto) {
        OrderResponse response = orderService.createOrder(userId, userEmail, dto);
        return ResponseEntity.ok(response);
    }
}
