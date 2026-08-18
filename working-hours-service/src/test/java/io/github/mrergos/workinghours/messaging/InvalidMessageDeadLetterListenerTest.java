package io.github.mrergos.workinghours.messaging;

import io.github.mrergos.workinghours.messaging.dto.InvalidMessageInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvalidMessageDeadLetterListener tests")
class InvalidMessageDeadLetterListenerTest {

    private final InvalidMessageDeadLetterListener listener = new InvalidMessageDeadLetterListener();

    @Test
    @DisplayName("onInvalidMessage: logs the invalid message without throwing")
    void onInvalidMessage_validPayload_shouldNotThrow() {
        //given
        InvalidMessageInfo invalidMessageInfo = new InvalidMessageInfo(
                "{\"trainerUsername\":null}",
                "workload.events",
                List.of("trainerUsername is required"),
                Instant.parse("2026-08-01T10:00:00Z"));

        //when
        //then
        assertDoesNotThrow(() -> listener.onInvalidMessage(invalidMessageInfo));
    }

    @Test
    @DisplayName("onInvalidMessage: handles multiple validation errors without throwing")
    void onInvalidMessage_multipleValidationErrors_shouldNotThrow() {
        //given
        InvalidMessageInfo invalidMessageInfo = new InvalidMessageInfo(
                "{}",
                "workload.events",
                List.of("trainerUsername is required", "trainingDate is required", "actionType is required"),
                Instant.now());

        //when
        //then
        assertDoesNotThrow(() -> listener.onInvalidMessage(invalidMessageInfo));
    }

    @Test
    @DisplayName("onInvalidMessage: handles an empty validation error list without throwing")
    void onInvalidMessage_emptyValidationErrors_shouldNotThrow() {
        //given
        InvalidMessageInfo invalidMessageInfo = new InvalidMessageInfo(
                "{}",
                "workload.events",
                List.of(),
                Instant.now());

        //when
        //then
        assertDoesNotThrow(() -> listener.onInvalidMessage(invalidMessageInfo));
    }

    @Test
    @DisplayName("onInvalidMessage: handles a null original payload without throwing")
    void onInvalidMessage_nullOriginalPayload_shouldNotThrow() {
        //given
        InvalidMessageInfo invalidMessageInfo = new InvalidMessageInfo(
                null,
                "workload.events",
                List.of("trainerUsername is required"),
                Instant.now());

        //when
        //then
        assertDoesNotThrow(() -> listener.onInvalidMessage(invalidMessageInfo));
    }
}
