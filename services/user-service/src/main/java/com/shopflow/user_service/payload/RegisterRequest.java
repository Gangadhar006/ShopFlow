package com.shopflow.user_service.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email String email,

    @NotBlank @Size(min = 8, message = "Password must be atleast 8 characters") String password
) {
}
