package com.shopflow.user_service.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String userId,
    String email,
    String role
) {
}
