package com.camss.honeymoney.dto;

public record LoginResponse(
    String token,
    UserSummary user
) {
    public record UserSummary(
        Long id,
        String name,
        String email
    ) {}
}