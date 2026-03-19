package com.shopflow.user_service.security;

import com.shopflow.user_service.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {JwtUtil.class})
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-minimum-32-characters-long",
    "jwt.access-token-expiry-ms=900000",
    "jwt.refresh-token-expiry-ms=604800000"
})
class JwtUtilTest {

    @Autowired
    JwtUtil jwtUtil;

    @Test
    void generateAndValidateAccessToken() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .email("a@b.com")
            .role(User.Role.CUSTOMER)
            .build();

        String token = jwtUtil.generateAccessToken(user);

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals(user.getId().toString(),
            jwtUtil.validateToken(token).getSubject());
    }

    @Test
    void invalidToken_returnsFalse() {
        assertFalse(jwtUtil.isTokenValid("not.a.token"));
    }
}
