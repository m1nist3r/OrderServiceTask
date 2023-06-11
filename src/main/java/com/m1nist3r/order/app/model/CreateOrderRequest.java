package com.m1nist3r.order.app.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank
        String productName,
        @Min(value = 1, message = "Minimal quantity for order must be at least 1")
        @Max(value = 99, message = "Maximal quantity for for order is 99")
        int quantity,
        @Min(value = 0, message = "Price can't be negative")
        double totalPrice
) {}
