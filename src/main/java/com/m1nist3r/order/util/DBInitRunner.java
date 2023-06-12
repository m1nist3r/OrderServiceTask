package com.m1nist3r.order.util;

import com.m1nist3r.order.app.entity.Order;
import com.m1nist3r.order.app.repository.OrderRepository;
import com.m1nist3r.order.security.entity.Permission;
import com.m1nist3r.order.security.entity.User;
import com.m1nist3r.order.security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class DBInitRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public DBInitRunner(UserRepository userRepository,
                        OrderRepository orderRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void run(String... args) {

        userRepository.save(
                new User(
                        "admin",
                        "admin@order.com",
                        passwordEncoder.encode("12345678"),
                        Instant.now(),
                        Instant.now(),
                        Instant.now(),
                        false,
                        List.of(Permission.ROLE_ADMIN)
        )).block();
        userRepository.save(
                new User(
                        "test01",
                        "test01@order.com",
                        passwordEncoder.encode("12345678"),
                        Instant.now(),
                        Instant.now(),
                        Instant.now(),
                        false,
                        List.of(Permission.ROLE_USER)
        )).block();

        orderRepository.save(
                new Order(
                        UUID.randomUUID().toString(),
                        "test01@order.com",
                        "product01",
                        1,
                        1.5

                )
        ).block();
    }
}
