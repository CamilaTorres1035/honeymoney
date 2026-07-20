package com.camss.honeymoney.dto;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    UserSummary user
) {
    public record UserSummary(
        Long id,
        String name,
        String email
    ) {}
}