package com.m1nist3r.order.security.auth;

import com.m1nist3r.order.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Component
public record UserAuthenticationManager(JwtService jwtService) implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        String username = jwtService.getUsernameFromToken(authToken);
        return Mono.just(jwtService.validateToken(authToken))
                .filter(valid -> valid)
                .switchIfEmpty(Mono.empty())
                .map(valid -> {
                    Claims claims = jwtService.getAllClaimsFromToken(authToken);
                    String[] roles = claims.get("role", String[].class);
                    return new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList()
                    );
                });
    }
}