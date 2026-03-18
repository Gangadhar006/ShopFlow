package com.shopflow.user_service.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
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
