package com.m1nist3r.order.security.repository;

import com.m1nist3r.order.security.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findUserByEmail(String email);
    Mono<Boolean> existsUserByEmail(String email);
}
