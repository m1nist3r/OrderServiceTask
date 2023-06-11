package com.m1nist3r.order.app.repository;

import com.m1nist3r.order.app.entity.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {

    Flux<Order> findAllByCustomerName(String username);
}
