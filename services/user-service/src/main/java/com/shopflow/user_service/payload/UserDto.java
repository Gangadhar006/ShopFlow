package com.shopflow.user_service.payload;

import com.shopflow.user_service.entity.User;
import lombok.Builder;

@Builder
public record UserDto(
    String id,
    String email,
    String role
) {
    public static UserDto from(User user) {
        return UserDto.builder()
            .email(user.getEmail())
            .id(user.getId().toString())
            .role(user.getRole().name())
            .build();
    }
}
