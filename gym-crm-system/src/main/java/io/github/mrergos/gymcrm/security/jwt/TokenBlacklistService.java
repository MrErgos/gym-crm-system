package io.github.mrergos.gymcrm.security.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(String tokenId, Instant expiresAt) {
        revokedTokens.put(tokenId, expiresAt);
        log.info("Token revoked, jti={}", tokenId);
    }

    public boolean isRevoked(String tokenId) {
        return revokedTokens.containsKey(tokenId);
    }

    @Scheduled(fixedRate = 60_000)
    public void evictExpiredEntries() {
        Instant now = Instant.now();
        int sizeBefore = revokedTokens.size();
        revokedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        int removed = sizeBefore - revokedTokens.size();
        if (removed > 0) {
            log.debug("Evicted {} expired blacklisted tokens", removed);
        }
    }
}
