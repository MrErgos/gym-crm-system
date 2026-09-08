package io.github.mrergos.workinghours.component.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class TestJwtIssuer {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public TestJwtIssuer( @Value("${security.jwt.secret}") String secret,
                          @Value("${security.jwt.expiration-ms}") long expirationMillis) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String issueValidToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(username)
                .id(jti)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }
}

