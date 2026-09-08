package io.github.mrergos.workinghours.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-32-bytes-long!";

    private JwtService jwtService;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    private String buildToken(String subject, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    @Test
    @DisplayName("isTokenValid returns true for a valid, non-expired token")
    void validTokenReturnsTrue() {
        String token = buildToken("service-account", new Date(System.currentTimeMillis() + 60_000));

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false for an expired token")
    void expiredTokenReturnsFalse() {
        String token = buildToken("service-account", new Date(System.currentTimeMillis() - 60_000));

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for a malformed token")
    void malformedTokenReturnsFalse() {
        assertThat(jwtService.isTokenValid("not-a-valid-jwt")).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false when signed with a different key")
    void tokenSignedWithDifferentKeyReturnsFalse() {
        SecretKey otherKey = Keys.hmacShaKeyFor("different-secret-key-of-32-bytes!!".getBytes());
        String token = Jwts.builder()
                .subject("service-account")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for null or empty token")
    void nullOrEmptyTokenReturnsFalse() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }

    @Test
    @DisplayName("extractSubject returns the correct subject from a valid token")
    void extractSubjectReturnsCorrectSubject() {
        String token = buildToken("John.Doe", new Date(System.currentTimeMillis() + 60_000));

        assertThat(jwtService.extractSubject(token)).isEqualTo("John.Doe");
    }
}
