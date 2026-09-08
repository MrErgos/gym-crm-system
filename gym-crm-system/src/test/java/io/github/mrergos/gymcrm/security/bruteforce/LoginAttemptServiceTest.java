package io.github.mrergos.gymcrm.security.bruteforce;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LoginAttemptService tests")
class LoginAttemptServiceTest {

    @Test
    @DisplayName("isBlocked: returns false for a username with no failed attempts")
    void isBlocked_noAttempts_shouldReturnFalse() {
        //given
        LoginAttemptService service = new LoginAttemptService(3, 15L);

        //when
        boolean result = service.isBlocked("John.Doe");

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("onFailedLogin: does not block before reaching maxAttempts")
    void onFailedLogin_belowMaxAttempts_shouldNotBlock() {
        //given
        LoginAttemptService service = new LoginAttemptService(3, 15L);

        //when
        service.onFailedLogin("John.Doe");
        service.onFailedLogin("John.Doe");

        //then
        assertFalse(service.isBlocked("John.Doe"));
    }

    @Test
    @DisplayName("onFailedLogin: blocks username after reaching maxAttempts")
    void onFailedLogin_reachesMaxAttempts_shouldBlock() {
        //given
        LoginAttemptService service = new LoginAttemptService(3, 15L);

        //when
        service.onFailedLogin("John.Doe");
        service.onFailedLogin("John.Doe");
        service.onFailedLogin("John.Doe");

        //then
        assertTrue(service.isBlocked("John.Doe"));
    }

    @Test
    @DisplayName("onSuccessfulLogin: resets failed attempts and lockout for username")
    void onSuccessfulLogin_afterLockout_shouldResetState() {
        //given
        LoginAttemptService service = new LoginAttemptService(3, 15L);
        service.onFailedLogin("John.Doe");
        service.onFailedLogin("John.Doe");
        service.onFailedLogin("John.Doe");

        //when
        service.onSuccessfulLogin("John.Doe");

        //then
        assertFalse(service.isBlocked("John.Doe"));
    }

    @Test
    @DisplayName("onFailedLogin: tracks usernames independently")
    void onFailedLogin_differentUsernames_shouldTrackIndependently() {
        //given
        LoginAttemptService service = new LoginAttemptService(3, 15L);

        //when
        service.onFailedLogin("John.Doe");
        service.onFailedLogin("John.Doe");
        service.onFailedLogin("John.Doe");
        service.onFailedLogin("Anna.Lee");

        //then
        assertTrue(service.isBlocked("John.Doe"));
        assertFalse(service.isBlocked("Anna.Lee"));
    }

    @Test
    @DisplayName("isBlocked: unblocks and clears state once lockout duration has passed")
    void isBlocked_lockoutExpired_shouldUnblockAndClearState() throws Exception {
        //given
        LoginAttemptService service = new LoginAttemptService(1, 0L);
        service.onFailedLogin("John.Doe");
        Thread.sleep(5);

        //when
        boolean result = service.isBlocked("John.Doe");

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("evictExpiredLockouts: removes expired lockouts so username is no longer blocked")
    void evictExpiredLockouts_expiredLockout_shouldRemoveEntry() throws Exception {
        //given
        LoginAttemptService service = new LoginAttemptService(1, 0L);
        service.onFailedLogin("John.Doe");
        Thread.sleep(5);

        //when
        service.evictExpiredLockouts();

        //then
        assertFalse(service.isBlocked("John.Doe"));
    }
}