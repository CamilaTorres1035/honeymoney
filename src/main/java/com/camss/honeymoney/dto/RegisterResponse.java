package com.camss.honeymoney.dto;

import java.time.Instant;

public record RegisterResponse(
    Long id,
    String name,
    String email,
    Instant createdAt
) {}

