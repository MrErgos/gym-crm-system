package io.github.mrergos.gymcrm.integration;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import io.jsonwebtoken.security.Keys;

@Component
public class ServiceTokenProvider {

    private static final String SERVICE_SUBJECT = "gym-crm-system";

    private final SecretKey signingKey;
    private final long expirationMillis;

    public ServiceTokenProvider(@Value("${security.jwt.secret}") String secret,
                                @Value("${security.jwt.expiration-ms}") long expirationMillis) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String bearerToken() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        String token = Jwts.builder()
                .subject(SERVICE_SUBJECT)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();

        return "Bearer " + token;
    }
}

