package com.m1nist3r.order.security.controller;

import com.m1nist3r.order.security.entity.Permission;
import com.m1nist3r.order.security.entity.User;
import com.m1nist3r.order.security.model.AuthenticationRequest;
import com.m1nist3r.order.security.jwt.JwtService;
import com.m1nist3r.order.security.model.AuthenticationResponse;
import com.m1nist3r.order.security.model.RegistrationRequest;
import com.m1nist3r.order.security.model.RegistrationResponse;
import com.m1nist3r.order.security.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("api/v1/auth")
@Tag(name = "User APIs", description = "User API")
public record AuthController(
        UserRepository userRepository,
        JwtService jwtService,
        PasswordEncoder passwordEncoder
) {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthenticationResponse>> login(
            @RequestBody AuthenticationRequest authenticationRequest
    ) {
        return this.userRepository.findUserByEmail(authenticationRequest.email())
                .filter(user -> passwordEncoder.matches(authenticationRequest.password(), user.password()))
                .map(user -> ResponseEntity.ok(new AuthenticationResponse(jwtService.generateToken(user))))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<?>> register(
            @Valid @RequestBody RegistrationRequest registrationRequest
    ) {
        var userId = UUID.randomUUID().toString();

        return userRepository.existsUserByEmail(registrationRequest.email())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(ResponseEntity.badRequest().body("User already exists."));
                    }

                    return userRepository.save(
                            new User(
                                    userId,
                                    registrationRequest.email(),
                                    passwordEncoder.encode(registrationRequest.password()),
                                    Instant.now(),
                                    Instant.now(),
                                    Instant.now(),
                                    false,
                                    List.of(Permission.ROLE_USER)
                            ))
                        .doOnError(throwable -> LOGGER.error("User cannot be saved due to exception occur. {}",
                                throwable.getLocalizedMessage()))
                        .doOnSuccess(user -> LOGGER.debug("User with Id: {} added.", user.id()))
                        .map(user -> ResponseEntity.ok(
                                new RegistrationResponse(userId)));
                });
    }
}