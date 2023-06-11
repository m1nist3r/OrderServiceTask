package com.m1nist3r.order.app.controller;

import com.m1nist3r.order.app.entity.Order;
import com.m1nist3r.order.app.model.CreateOrderRequest;
import com.m1nist3r.order.app.service.OrderService;
import com.m1nist3r.order.security.auth.SecurityContextRepository;
import com.m1nist3r.order.security.auth.UserAuthenticationManager;
import com.m1nist3r.order.security.configuration.SecurityConfiguration;
import com.m1nist3r.order.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@AutoConfigureWebTestClient(timeout = "10000")
@WebFluxTest(controllers = OrderController.class)
@Import({JwtService.class, UserAuthenticationManager.class,
        SecurityContextRepository.class, SecurityConfiguration.class})
public class OrderControllerTest {

    private final String ORDER_URI = "/api/v1/orders";
    private final String USERNAME = "test@test.com";

    @Autowired
    public WebTestClient webTestClient;
    @MockBean
    private OrderService orderService;

    public static Stream<Arguments> invalidInput() {
        return Stream.of(
                Arguments.arguments(new CreateOrderRequest("", 0, -1)),
                Arguments.arguments(new CreateOrderRequest("testProduct", 0, -1)),
                Arguments.arguments(new CreateOrderRequest("testProduct", 1, -1)),
                Arguments.arguments(new CreateOrderRequest("testProduct", 0, 1)),
                Arguments.arguments(new CreateOrderRequest("", 1, 1))

        );
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "USER")
    void createOrderTest() {
        var createOrderRequest = new CreateOrderRequest("milkshake", 2, 10d);
        when(orderService.createOrder(createOrderRequest, USERNAME))
                .thenReturn(Mono.just("testOrderId"));

        webTestClient.post()
                .uri(ORDER_URI)
                .body(Mono.just(createOrderRequest), CreateOrderRequest.class)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(String.class)
                .value(orderId -> assertEquals("testOrderId", orderId));
    }

    @ParameterizedTest
    @MethodSource("invalidInput")
    @WithMockUser(username = USERNAME, roles = "USER")
    void createOrderInvalidInputTest(CreateOrderRequest createOrderRequest) {
        webTestClient.post()
                .uri(ORDER_URI)
                .body(Mono.just(createOrderRequest), CreateOrderRequest.class)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "USER")
    void getUserOrdersTest() {
        var expectedOrder = new Order(
                "testOrderId",
                USERNAME,
                "milkshake",
                1,
                1.5d
        );
        when(orderService.getUserOrders(USERNAME, USERNAME, List.of("ROLE_USER")))
                .thenReturn(Flux.just(expectedOrder));
        webTestClient.get()
                .uri(ORDER_URI + String.format("/%s", USERNAME))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Order.class)
                .contains(expectedOrder);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "ADMIN")
    void getOrdersTest() {
        var expectedOrder = new Order(
                "testOrderId",
                USERNAME,
                "milkshake",
                1,
                1.5d
        );
        when(orderService.getOrders())
                .thenReturn(Flux.just(expectedOrder));
        webTestClient.get()
                .uri(ORDER_URI)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Order.class)
                .contains(expectedOrder);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "USER")
    void getOrdersWrongRoleTest() {
        webTestClient.get()
                .uri(ORDER_URI)
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "ADMIN")
    void removeOrderTest() {
        when(orderService.removeOrder("testOrderId")).thenReturn(Mono.empty());
        webTestClient.delete()
                .uri(ORDER_URI + "/testOrderId")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "USER")
    void removeOrderWrongRoleTest() {
        webTestClient.delete()
                .uri(ORDER_URI + "/testOrderId")
                .exchange()
                .expectStatus()
                .isForbidden();
    }
}
