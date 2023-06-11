package com.m1nist3r.order.security.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RegistrationRequest(
        @NotBlank
        @Size(min = 8, message = "Password must contain minimum 8 characters.")
        @Size(max = 32, message = "Password must contain maximum 32 characters.")
        String password,
        @NotBlank
        @Email
        String email) {
}
