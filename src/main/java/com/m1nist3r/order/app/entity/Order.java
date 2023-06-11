package com.m1nist3r.order.app.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "order")
public record Order(@Id String id, String customerName, String productName, int quantity, double totalPrice) {}
