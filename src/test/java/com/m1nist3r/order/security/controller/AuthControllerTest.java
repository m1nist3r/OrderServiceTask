package com.m1nist3r.order.security.controller;

import com.m1nist3r.order.security.auth.SecurityContextRepository;
import com.m1nist3r.order.security.auth.UserAuthenticationManager;
import com.m1nist3r.order.security.configuration.SecurityConfiguration;
import com.m1nist3r.order.security.entity.Permission;
import com.m1nist3r.order.security.entity.User;
import com.m1nist3r.order.security.jwt.JwtService;
import com.m1nist3r.order.security.model.AuthenticationRequest;
import com.m1nist3r.order.security.model.AuthenticationResponse;
import com.m1nist3r.order.security.model.RegistrationRequest;
import com.m1nist3r.order.security.model.RegistrationResponse;
import com.m1nist3r.order.security.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@AutoConfigureWebTestClient(timeout = "10000")
@WebFluxTest(controllers = AuthController.class)
@Import({JwtService.class, UserAuthenticationManager.class,
        SecurityContextRepository.class, SecurityConfiguration.class})
public class AuthControllerTest {

    private final String AUTH_URI = "/api/v1/auth";

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private UserRepository userRepository;

    public static Stream<Arguments> invalidInput() {
        return Stream.of(
                Arguments.arguments(new RegistrationRequest("", "")),
                Arguments.arguments(new RegistrationRequest("12345678", "")),
                Arguments.arguments(new RegistrationRequest("", "test@test.com")),
                Arguments.arguments(new RegistrationRequest("12", "test@test.com")),
                Arguments.arguments(new RegistrationRequest("12345678", "test.com"))
        );
    }

    @Test
    void registerTest() {
        var registrationRequest = new RegistrationRequest(
                "12345678", "test@test.com"
        );
        when(userRepository.existsUserByEmail(registrationRequest.email()))
                .thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class)))
                .thenReturn(Mono.just(new User(
                        "testId",
                        "test@test.com",
                        passwordEncoder.encode("12345678"),
                        Instant.now(),
                        Instant.now(),
                        Instant.now(),
                        false,
                        List.of(Permission.USER)
                )));

        webTestClient.post()
                .uri(AUTH_URI + "/register")
                .body(Mono.just(registrationRequest), RegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(RegistrationResponse.class);
    }

    @ParameterizedTest
    @MethodSource(value = "invalidInput")
    void registerInvalidInputTest(RegistrationRequest registrationRequest) {
        webTestClient.post()
                .uri(AUTH_URI + "/register")
                .body(Mono.just(registrationRequest), RegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody();
    }

    @Test
    void loginTest() {
        var authenticationRequest = new AuthenticationRequest("test@test.com", "12345678");
        when(userRepository.findUserByEmail(authenticationRequest.email()))
                .thenReturn(Mono.just(new User(
                            "testId",
                            "test@test.com",
                            passwordEncoder.encode("12345678"),
                            Instant.now(),
                            Instant.now(),
                            Instant.now(),
                            false,
                            List.of(Permission.USER)
                        )
                ));
        webTestClient.post()
                .uri(AUTH_URI + "/login")
                .body(Mono.just(authenticationRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AuthenticationResponse.class);
    }

    @Test
    void loginBadCredentialsTest() {
        var authenticationRequest = new AuthenticationRequest("test@test.com", "87654321");
        when(userRepository.findUserByEmail(authenticationRequest.email()))
                .thenReturn(Mono.just(new User(
                            "testId",
                            "test@test.com",
                            passwordEncoder.encode("12345678"),
                            Instant.now(),
                            Instant.now(),
                            Instant.now(),
                            false,
                            List.of(Permission.USER)
                        )
                ));
        webTestClient.post()
                .uri(AUTH_URI + "/login")
                .body(Mono.just(authenticationRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
