package com.trung.orderservice.service;

import com.trung.orderservice.dto.OrderCreateRequest;
import com.trung.orderservice.dto.OrderResponse;
import com.trung.orderservice.dto.ProductResponse;
import com.trung.orderservice.entity.OrderStatus;
import com.trung.orderservice.entity.Orders;
import com.trung.orderservice.event.OrderCreateEvent;
import com.trung.orderservice.repository.OrderRepository;
import com.trung.orderservice.service.client.ProductClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Transactional
    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackCreateOrder")
    public OrderResponse createOrder(Long userId, String userEmail, OrderCreateRequest dto){
        if (productClient.getProductById(dto.getProductId()).getBody() == null) {
            throw new RuntimeException("Product not found with id: " + dto.getProductId());
        }

        log.info("Product found with id: " + dto.getProductId());

        ProductResponse productResponse = productClient.getProductById(dto.getProductId()).getBody();

        productClient.reduceQuantity(dto.getProductId(), dto.getQuantity());
        System.out.println("Product quantity reduced " + dto.getQuantity() + " for product " + dto.getProductId());

        BigDecimal totalPrice = productResponse.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));

        Orders orders = new Orders();
        orders.setProductId(dto.getProductId());
        orders.setQuantity(dto.getQuantity());
        orders.setTotalPrice(totalPrice);
        orders.setStatus(OrderStatus.PENDING);
        orders.setUserId(userId);
        orderRepository.save(orders);

        OrderCreateEvent event = OrderCreateEvent.builder()
                .orderId(orders.getId())
                .productId(dto.getProductId())
                .productName(productResponse.getName())
                .quantity(dto.getQuantity())
                .userId(userId)
                .totalPrice(totalPrice)
                .userEmail(userEmail)
                .build();
        kafkaTemplate.send("order-create-topic", event);
        return OrderResponse.builder()
                .id(orders.getId())
                .userId(userId)
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .status(orders.getStatus().name())
                .totalPrice(totalPrice)
                .build();
    }

    public OrderResponse fallbackCreateOrder(Long userId, OrderCreateRequest dto, Throwable throwable){
        // Log the error or perform any necessary actions
        System.out.println("Fallback method called due to: " + throwable.getMessage());

        // Return a default response or throw an exception
        throw new RuntimeException("Unable to create order at the moment. Please try again later.");
    }
}
