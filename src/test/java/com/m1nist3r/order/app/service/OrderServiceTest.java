package com.m1nist3r.order.app.service;

import com.m1nist3r.order.app.entity.Order;
import com.m1nist3r.order.app.model.CreateOrderRequest;
import com.m1nist3r.order.app.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public OrderServiceTest() {
        this.orderRepository = mock(OrderRepository.class);
        this.orderService = new OrderService(orderRepository);
    }

    @Test
    void createOrderTest() {
        var createOrderRequest = new CreateOrderRequest("milkshake", 2, 10d);
        var username = "test@test.com";

        orderService.createOrder(createOrderRequest, username).block();

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void getUserOrdersTest() {
        var authenticatedName = "test@test.com";
        var roles = List.of("USER");
        var expectedOrder = new Order(
                "testOrderId",
                authenticatedName,
                "milkshake",
                1,
                1.5d
        );

        when(orderRepository.findAllByCustomerName(anyString()))
                .thenReturn(Flux.just(expectedOrder));

        var orders = orderService.getUserOrders("test", authenticatedName, roles).toStream().toList();
        assertEquals(0, orders.size());
        verify(orderRepository, times(0)).findAllByCustomerName("test");

        orders = orderService.getUserOrders(authenticatedName, authenticatedName, roles).toStream().toList();
        assertEquals(1, orders.size());
        verify(orderRepository, times(1)).findAllByCustomerName(authenticatedName);

        roles = List.of("ADMIN");
        orders = orderService.getUserOrders("test", authenticatedName, roles).toStream().toList();
        assertEquals(1, orders.size());
        verify(orderRepository, times(1)).findAllByCustomerName("test");
    }
}
