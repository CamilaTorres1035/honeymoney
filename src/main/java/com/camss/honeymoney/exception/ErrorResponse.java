package com.camss.honeymoney.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldError> details
) {
    public record FieldError(String field, String issue) {}

    // Constructor de conveniencia para errores sin detalles
    public ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }
}