package com.tarakki.boardtask.config;

import com.tarakki.boardtask.util.JwtTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    private JwtDecoder jwtDecoder;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        SecurityConfig securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "jwtSecret", JwtTestDataFactory.SECRET);
        jwtDecoder = securityConfig.jwtDecoder();

        memberId = UUID.randomUUID();
    }

    @Test
    void jwtDecoder_shouldDecodeValidTokenAndExposeMemberIdAsSubject() {
        String token = JwtTestDataFactory.createValidToken(memberId);

        Jwt jwt = jwtDecoder.decode(token);

        assertNotNull(jwt);
        assertEquals(memberId.toString(), jwt.getSubject());
        assertNotNull(jwt.getExpiresAt());
    }

    @Test
    void jwtDecoder_shouldRejectExpiredToken() {
        String token = JwtTestDataFactory.createExpiredToken(memberId);

        JwtValidationException exception = assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));

        assertTrue(exception.getMessage().toLowerCase().contains("expired"));
    }

    @Test
    void jwtDecoder_shouldRejectTokenSignedWithDifferentSecret() {
        String token = JwtTestDataFactory.createTokenSignedWithOtherSecret(memberId);

        BadJwtException exception = assertThrows(BadJwtException.class, () -> jwtDecoder.decode(token));

        assertTrue(exception.getMessage().toLowerCase().contains("signature"));
    }

    @Test
    void jwtDecoder_shouldRejectTokenWhosePayloadWasTamperedWith() {
        String token = JwtTestDataFactory.createTamperedToken(memberId, UUID.randomUUID());

        BadJwtException exception = assertThrows(BadJwtException.class, () -> jwtDecoder.decode(token));

        assertTrue(exception.getMessage().toLowerCase().contains("signature"));
    }

    @Test
    void jwtDecoder_shouldRejectMalformedToken() {
        assertThrows(BadJwtException.class, () -> jwtDecoder.decode("not-a-jwt"));
    }
}
