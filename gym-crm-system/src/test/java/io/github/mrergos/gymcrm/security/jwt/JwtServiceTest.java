package io.github.mrergos.gymcrm.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JwtService tests")
class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-key-that-is-long-enough-for-hs256-signing";

    @Test
    @DisplayName("generateToken then extractUsername: returns the original username")
    void generateToken_thenExtractUsername_shouldReturnOriginalUsername() {
        //given
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000L);

        //when
        String token = jwtService.generateToken("John.Doe");

        //then
        assertNotNull(token);
        assertEquals("John.Doe", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("generateToken: issues tokens with unique token ids")
    void generateToken_calledTwice_shouldProduceUniqueTokenIds() {
        //given
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000L);

        //when
        String tokenOne = jwtService.generateToken("John.Doe");
        String tokenTwo = jwtService.generateToken("John.Doe");

        //then
        assertFalse(jwtService.extractTokenId(tokenOne).equals(jwtService.extractTokenId(tokenTwo)));
    }

    @Test
    @DisplayName("extractExpiration: returns a future expiration date right after issuance")
    void extractExpiration_freshToken_shouldBeInTheFuture() {
        //given
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000L);
        String token = jwtService.generateToken("John.Doe");

        //when
        Date expiration = jwtService.extractExpiration(token);

        //then
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("isTokenValid: returns true for a fresh token and matching username")
    void isTokenValid_freshTokenMatchingUsername_shouldReturnTrue() {
        //given
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000L);
        String token = jwtService.generateToken("John.Doe");

        //when
        boolean result = jwtService.isTokenValid(token, "John.Doe");

        //then
        assertTrue(result);
    }

    @Test
    @DisplayName("isTokenValid: returns false when username does not match")
    void isTokenValid_usernameMismatch_shouldReturnFalse() {
        //given
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000L);
        String token = jwtService.generateToken("John.Doe");

        //when
        boolean result = jwtService.isTokenValid(token, "Someone.Else");

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("isTokenValid: returns false for an already-expired token")
    void isTokenValid_expiredToken_shouldReturnFalse() throws InterruptedException {
        //given
        JwtService jwtService = new JwtService(TEST_SECRET, 1L);
        String token = jwtService.generateToken("John.Doe");
        Thread.sleep(10);

        //when
        boolean result = jwtService.isTokenValid(token, "John.Doe");

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("isTokenValid: returns false for a malformed token")
    void isTokenValid_malformedToken_shouldReturnFalse() {
        //given
        JwtService jwtService = new JwtService(TEST_SECRET, 60_000L);

        //when
        boolean result = jwtService.isTokenValid("not-a-real-jwt", "John.Doe");

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("isTokenValid: returns false for a token signed with a different key")
    void isTokenValid_differentSigningKey_shouldReturnFalse() {
        //given
        JwtService issuer = new JwtService("another-completely-different-secret-key-value", 60_000L);
        JwtService verifier = new JwtService(TEST_SECRET, 60_000L);
        String token = issuer.generateToken("John.Doe");

        //when
        boolean result = verifier.isTokenValid(token, "John.Doe");

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("extractUsername: throws for an expired token")
    void extractUsername_expiredToken_shouldThrow() throws InterruptedException {
        //given
        JwtService jwtService = new JwtService(TEST_SECRET, 1L);
        String token = jwtService.generateToken("John.Doe");
        Thread.sleep(10);

        //when
        //then
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
    }
}