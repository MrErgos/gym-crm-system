package io.github.mrergos.gymcrm.security.bruteforce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final int maxAttempts;
    private final Duration lockoutDuration;

    private final Map<String, AtomicInteger> attemptsByUsername = new ConcurrentHashMap<>();
    private final Map<String, Instant> lockedUntilByUsername = new ConcurrentHashMap<>();

    public LoginAttemptService(@Value("${security.brute-force.max-attempts}") int maxAttempts,
                                @Value("${security.brute-force.lockout-minutes}") long lockoutMinutes) {
        this.maxAttempts = maxAttempts;
        this.lockoutDuration = Duration.ofMinutes(lockoutMinutes);
    }

    public void onSuccessfulLogin(String username) {
        attemptsByUsername.remove(username);
        lockedUntilByUsername.remove(username);
        log.debug("Login attempt counters reset for username={}", username);
    }

    public void onFailedLogin(String username) {
        int attempts = attemptsByUsername
                .computeIfAbsent(username, key -> new AtomicInteger(0))
                .incrementAndGet();

        log.warn("Failed login attempt {}/{} for username={}", attempts, maxAttempts, username);

        if (attempts >= maxAttempts) {
            Instant lockedUntil = Instant.now().plus(lockoutDuration);
            lockedUntilByUsername.put(username, lockedUntil);
            log.warn("Username={} locked out until {} after {} failed attempts", username, lockedUntil, attempts);
        }
    }

    public boolean isBlocked(String username) {
        Instant lockedUntil = lockedUntilByUsername.get(username);
        if (lockedUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(lockedUntil)) {
            lockedUntilByUsername.remove(username);
            attemptsByUsername.remove(username);
            log.debug("Lockout expired for username={}, unblocking", username);
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 60_000)
    public void evictExpiredLockouts() {
        Instant now = Instant.now();
        lockedUntilByUsername.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isBefore(now);
            if (expired) {
                attemptsByUsername.remove(entry.getKey());
            }
            return expired;
        });
    }
}
