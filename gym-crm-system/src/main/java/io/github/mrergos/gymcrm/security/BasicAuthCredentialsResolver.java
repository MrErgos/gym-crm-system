package io.github.mrergos.gymcrm.security;

import io.github.mrergos.gymcrm.exception.AuthenticationException;
import io.github.mrergos.gymcrm.facade.Credentials;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class BasicAuthCredentialsResolver {
    private static final String BASIC_PREFIX = "Basic ";

    public Credentials resolve(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith(BASIC_PREFIX)) {
            throw new AuthenticationException("Missing Authorization header");
        }

        String base64Credentials = header.substring(BASIC_PREFIX.length()).trim();
        String decode;

        try {
            decode = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid Base64 in Authorization header");
        }

        int separatorIndex = decode.indexOf(':');
        if (separatorIndex == -1) {
            throw new AuthenticationException("Invalid Authorization header format");
        }

        String username = decode.substring(0, separatorIndex);
        String password = decode.substring(separatorIndex + 1);

        if (username.isBlank() || password.isBlank()) {
            throw new AuthenticationException("Username and password must not be blank");
        }

        return new Credentials(username, password);
    }
}
