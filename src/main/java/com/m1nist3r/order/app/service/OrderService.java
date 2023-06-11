package com.m1nist3r.order.app.service;

import com.m1nist3r.order.app.entity.Order;
import com.m1nist3r.order.app.model.CreateOrderRequest;
import com.m1nist3r.order.app.repository.OrderRepository;
import com.m1nist3r.order.security.entity.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public record OrderService(OrderRepository orderRepository) {
    private final static Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    public Mono<String> createOrder(CreateOrderRequest createOrderRequest, String username) {
        var id = UUID.randomUUID().toString();
        var order = new Order(
                id,
                username,
                createOrderRequest.productName(),
                createOrderRequest.quantity(),
                createOrderRequest.totalPrice()
        );

        return orderRepository.save(order).map(Order::id)
                .doOnError(throwable -> LOGGER.error("Order cannot be created due to exception occur. {}",
                        throwable.getLocalizedMessage()))
                .doOnSuccess(orderId -> LOGGER.debug("Order with Id: {} created.", orderId));
    }

    public Flux<Order> getUserOrders(String username, String authenticatedName, List<String> roles) {
        if (!roles.contains(Permission.ADMIN.name().toLowerCase())) {
            if (!username.equals(authenticatedName)) {
                return Flux.empty();
            }
        }
        return orderRepository.findAllByCustomerName(username);
    }

    public Flux<Order> getOrders() {
        return orderRepository.findAll();
    }

    public Mono<Void> removeOrder(String orderId) {
        return orderRepository.deleteById(orderId)
                .doOnError(throwable -> LOGGER.error("Order cannot be created due to exception occur. {}",
                        throwable.getLocalizedMessage()))
                .doOnSuccess(unused -> LOGGER.debug("Order with Id: {} created.", orderId));
    }
}
