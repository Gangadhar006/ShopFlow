package com.shopflow.user_service.service;

import com.shopflow.user_service.entity.User;
import com.shopflow.user_service.exception.UserAlreadyExistsException;
import com.shopflow.user_service.dto.AuthResponse;
import com.shopflow.user_service.dto.LoginRequest;
import com.shopflow.user_service.dto.RegisterRequest;
import com.shopflow.user_service.repository.UserRepository;
import com.shopflow.user_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtUtil jwtUtil;
    @InjectMocks
    UserService userService;

    @Test
    void register_newUser_returnsTokens() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtUtil.generateAccessToken(any())).thenReturn("access");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh");

        AuthResponse res = userService.register(
            new RegisterRequest("a@b.com", "password123"));

        assertEquals("access", res.accessToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
            () -> userService.register(
                new RegisterRequest("a@b.com", "password123")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_wrongPassword_throws() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .email("a@b.com")
            .password("hashed")
            .role(User.Role.CUSTOMER)
            .build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(BadCredentialsException.class,
            () -> userService.login(
                new LoginRequest("a@b.com", "wrongpass")));
    }
}
