package com.shopflow.user_service.service;

import com.shopflow.user_service.entity.User;
import com.shopflow.user_service.exception.UserAlreadyExistsException;
import com.shopflow.user_service.dto.AuthResponse;
import com.shopflow.user_service.dto.LoginRequest;
import com.shopflow.user_service.dto.RegisterRequest;
import com.shopflow.user_service.dto.UserDto;
import com.shopflow.user_service.repository.UserRepository;
import com.shopflow.user_service.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .role(User.Role.CUSTOMER)
            .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public UserDto getMe(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return UserDto.from(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
            .accessToken(jwtUtil.generateAccessToken(user))
            .refreshToken(jwtUtil.generateRefreshToken(user))
            .userId(user.getId().toString())
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }
}
