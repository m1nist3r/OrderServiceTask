package com.m1nist3r.order.security.configuration;

import com.m1nist3r.order.security.auth.SecurityContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Configuration
public class SecurityConfiguration {

    private final ReactiveAuthenticationManager reactiveAuthenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Autowired
    public SecurityConfiguration(ReactiveAuthenticationManager reactiveAuthenticationManager,
                                 SecurityContextRepository securityContextRepository) {
        this.reactiveAuthenticationManager = reactiveAuthenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    private static final String[] AUTH_WHITELIST = {
            "/favicon.ico",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/v3/**"
    };

    @Bean
    protected SecurityWebFilterChain configure(ServerHttpSecurity http) {
        return http
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint((swe, e) ->
                                Mono.fromRunnable(() ->
                                        swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
                        .accessDeniedHandler((swe, e) ->
                                Mono.fromRunnable(() ->
                                        swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN))
                        ))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authenticationManager(reactiveAuthenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange(authorizeExchangeSpec ->
                        authorizeExchangeSpec.pathMatchers(HttpMethod.OPTIONS).permitAll()
                                .pathMatchers(AUTH_WHITELIST).permitAll()
                                .anyExchange().authenticated())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
}
