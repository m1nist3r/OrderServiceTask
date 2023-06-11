package com.m1nist3r.order.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record User(
        @Id
        String id,
        @Indexed(unique = true)
        String email,
        @JsonIgnore
        String password,
        Instant creationDate,
        Instant modificationDate,
        Instant lastActivityDate,
        @JsonIgnore
        boolean isDeleted,
        List<Permission> permissions
) {}
