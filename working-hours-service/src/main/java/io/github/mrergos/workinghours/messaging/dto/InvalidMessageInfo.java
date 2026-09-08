package io.github.mrergos.workinghours.messaging.dto;

import java.time.Instant;
import java.util.List;

public record InvalidMessageInfo(
        String originalPayload,
        String sourceQueue,
        List<String> validationErrors,
        Instant receivedAt
) {
}
