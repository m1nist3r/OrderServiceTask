package com.m1nist3r.order.security.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public record SecurityContextRepository(ReactiveAuthenticationManager reactiveAuthenticationManager)
        implements ServerSecurityContextRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityContextRepository.class);

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange swe) {
        return Mono.justOrEmpty(swe.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .flatMap(authHeader -> {
                    var authToken = authHeader.substring(7);
                    var auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
                    return this.reactiveAuthenticationManager.authenticate(auth)
                            .<SecurityContext>map(SecurityContextImpl::new);
                })
                .onErrorResume(throwable -> {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(throwable.getLocalizedMessage());
                    }
                    return Mono.empty();
                });
    }
}