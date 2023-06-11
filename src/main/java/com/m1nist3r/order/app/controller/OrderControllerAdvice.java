package com.m1nist3r.order.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(assignableTypes = OrderController.class)
public record OrderControllerAdvice() {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<?> handle(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        return ResponseEntity.badRequest().body(getErrorsMap(errors));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<?> handle(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getLocalizedMessage());
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }
}
