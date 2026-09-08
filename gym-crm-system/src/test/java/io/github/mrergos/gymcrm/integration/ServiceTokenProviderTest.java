package io.github.mrergos.gymcrm.integration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceTokenProvider tests")
class ServiceTokenProviderTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256!!";
    private static final long EXPIRATION_MS = 60_000L;

    private ServiceTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ServiceTokenProvider(SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("bearerToken: returns string with Bearer prefix")
    void bearerToken_valid_shouldStartWithBearerPrefix() {
        //when
        String token = provider.bearerToken();

        //then
        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "));
    }

    @Test
    @DisplayName("bearerToken: produces parsable JWT with expected subject and expiry")
    void bearerToken_valid_shouldContainExpectedClaims() {
        //given
        SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant before = Instant.now();

        //when
        String token = provider.bearerToken().substring("Bearer ".length());

        //then
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("gym-crm-system", claims.getSubject());
        assertDoesNotThrow(() -> UUID.fromString(claims.getId()));

        long expectedExpiryMillis = before.toEpochMilli() + EXPIRATION_MS;
        long actualExpiryMillis = claims.getExpiration().getTime();
        assertTrue(Math.abs(actualExpiryMillis - expectedExpiryMillis) < 5000,
                "Expiration should be close to issuedAt + expirationMillis");
    }

    @Test
    @DisplayName("bearerToken: generates a different token id on each call")
    void bearerToken_calledTwice_shouldProduceDifferentTokenIds() {
        //given
        SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        //when
        String token1 = provider.bearerToken().substring("Bearer ".length());
        String token2 = provider.bearerToken().substring("Bearer ".length());

        //then
        String jti1 = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token1).getPayload().getId();
        String jti2 = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token2).getPayload().getId();

        assertNotEquals(jti1, jti2);
    }
}