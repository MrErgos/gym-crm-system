package io.github.mrergos.gymcrm.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TokenBlacklistService tests")
class TokenBlacklistServiceTest {

    private final TokenBlacklistService service = new TokenBlacklistService();

    @Test
    @DisplayName("isRevoked: returns false for a token that was never revoked")
    void isRevoked_unknownToken_shouldReturnFalse() {
        //given
        //when
        boolean result = service.isRevoked("unknown-jti");

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("revoke then isRevoked: returns true for a revoked token")
    void revoke_thenIsRevoked_shouldReturnTrue() {
        //given
        service.revoke("jti-123", Instant.now().plusSeconds(3600));

        //when
        boolean result = service.isRevoked("jti-123");

        //then
        assertTrue(result);
    }

    @Test
    @DisplayName("evictExpiredEntries: removes tokens whose expiry is in the past")
    void evictExpiredEntries_expiredToken_shouldBeRemoved() {
        //given
        service.revoke("expired-jti", Instant.now().minusSeconds(10));

        //when
        service.evictExpiredEntries();

        //then
        assertFalse(service.isRevoked("expired-jti"));
    }

    @Test
    @DisplayName("evictExpiredEntries: keeps tokens whose expiry is in the future")
    void evictExpiredEntries_notYetExpiredToken_shouldRemain() {
        //given
        service.revoke("valid-jti", Instant.now().plusSeconds(3600));

        //when
        service.evictExpiredEntries();

        //then
        assertTrue(service.isRevoked("valid-jti"));
    }
}