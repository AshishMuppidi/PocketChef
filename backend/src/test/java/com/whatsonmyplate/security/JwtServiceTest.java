package com.whatsonmyplate.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "unitTestingSecretKeyThatIsAtLeast256BitsLongForHmacSha256SignatureVerification!";
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    void testGenerateAndExtractToken() {
        UserDetails userDetails = new User(
                "user@example.com",
                "hashedpassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateToken(userDetails, 123L);

        assertNotNull(token);
        assertEquals("user@example.com", jwtService.extractUsername(token));
        assertEquals(123L, jwtService.extractUserId(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertTrue(jwtService.isTokenValid(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testTokenExpiration() {
        // Create a JwtService with 1ms expiration for expired test
        JwtService shortLivedJwtService = new JwtService(SECRET, 1);
        UserDetails userDetails = new User(
                "expired@example.com",
                "hashedpassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = shortLivedJwtService.generateToken(userDetails, 456L);

        // Sleep 10ms to ensure expiration
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }

        assertTrue(shortLivedJwtService.isTokenExpired(token));
        assertFalse(shortLivedJwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.jwt.token"));
    }
}
