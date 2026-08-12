package io.github.mrergos.gymcrm.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                       @Value("${security.jwt.expiration-ms}") long expirationMillis) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .subject(username)
                .id(jti)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();

        log.debug("Issued JWT for username={}, jti={}, expiresAt={}", username, jti, expiry);
        return token;
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractTokenId(String token) {
        return parseClaims(token).getId();
    }

    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        try {
            Claims claims = parseClaims(token);
            boolean usernameMatches = claims.getSubject().equals(expectedUsername);
            boolean notExpired = claims.getExpiration().after(new Date());
            return usernameMatches && notExpired;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed");
            throw e;
        }
    }
}
