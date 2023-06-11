package com.m1nist3r.order.app.controller;

import com.m1nist3r.order.app.entity.Order;
import com.m1nist3r.order.app.model.CreateOrderRequest;
import com.m1nist3r.order.app.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Mono<ResponseEntity<String>> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest,
            Authentication authentication
    ) {
        return orderService.createOrder(createOrderRequest, authentication.getName())
                .map(orderId -> ResponseEntity.status(HttpStatus.CREATED).body(orderId));
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Flux<Order> getUserOrders(@PathVariable String username, Authentication authentication) {
        var roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return orderService.getUserOrders(username, authentication.getName(), roles);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<Order> getOrders() {
        return orderService.getOrders();
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<?>> removeOrder(@PathVariable String orderId) {
        return orderService.removeOrder(orderId).then(Mono.fromCallable(() -> ResponseEntity.ok().build()));
    }
}
