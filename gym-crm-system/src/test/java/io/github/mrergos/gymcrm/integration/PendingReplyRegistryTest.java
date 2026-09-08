package io.github.mrergos.gymcrm.integration;

import io.github.mrergos.gymcrm.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.gymcrm.integration.dto.WorkloadSummaryReply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PendingReplyRegistry tests")
class PendingReplyRegistryTest {

    private PendingReplyRegistry registry;

    private WorkloadSummaryReply buildReply() {
        return WorkloadSummaryReply.of(
                new TrainerWorkloadSummaryResponse("Jane.Smith", "Jane",
                        "Smith", true, List.of()));
    }

    @BeforeEach
    void setUp() {
        registry = new PendingReplyRegistry();
    }

    @Test
    @DisplayName("register: returns a registration with a non-null correlation id and an incomplete future")
    void register_shouldReturnUniqueCorrelationIdAndIncompleteFuture() {
        //given
        //when
        PendingReplyRegistry.Registration registration = registry.register();

        //then
        assertNotNull(registration.correlationId());
        assertFalse(registration.future().isDone());
    }

    @Test
    @DisplayName("register: generates a different correlation id on every call")
    void register_calledTwice_shouldReturnDifferentCorrelationIds() {
        //given
        //when
        PendingReplyRegistry.Registration first = registry.register();
        PendingReplyRegistry.Registration second = registry.register();

        //then
        assertNotEquals(first.correlationId(), second.correlationId());
    }

    @Test
    @DisplayName("complete: completes the future for a known correlation id with the given reply")
    void complete_knownCorrelationId_shouldCompleteFuture() throws Exception {
        //given
        PendingReplyRegistry.Registration registration = registry.register();
        WorkloadSummaryReply reply = buildReply();

        //when
        registry.complete(registration.correlationId(), reply);

        //then
        assertTrue(registration.future().isDone());
        assertEquals(reply, registration.future().get(1, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("complete: does nothing and does not throw for an unknown correlation id")
    void complete_unknownCorrelationId_shouldDoNothing() {
        //given
        PendingReplyRegistry.Registration registration = registry.register();
        WorkloadSummaryReply reply = buildReply();

        //when
        registry.complete("some-other-correlation-id", reply);

        //then
        assertFalse(registration.future().isDone());
    }

    @Test
    @DisplayName("complete: does nothing and does not throw for a null correlation id")
    void complete_nullCorrelationId_shouldDoNothing() {
        //given
        PendingReplyRegistry.Registration registration = registry.register();
        WorkloadSummaryReply reply = buildReply();

        //when
        registry.complete(null, reply);

        //then
        assertFalse(registration.future().isDone());
    }

    @Test
    @DisplayName("remove: is idempotent and safe to call multiple times for the same correlation id")
    void remove_calledTwice_shouldNotThrow() {
        //given
        PendingReplyRegistry.Registration registration = registry.register();

        registry.remove(registration.correlationId());
        registry.remove(registration.correlationId());

        //when
        registry.complete(registration.correlationId(), buildReply());

        //then
        assertFalse(registration.future().isDone());
    }

    @Test
    @DisplayName("remove: only removes the targeted registration, leaving others completable")
    void remove_shouldOnlyAffectTargetedRegistration() throws Exception {
        //given
        PendingReplyRegistry.Registration first = registry.register();
        PendingReplyRegistry.Registration second = registry.register();

        //when
        registry.remove(first.correlationId());
        WorkloadSummaryReply reply = buildReply();
        registry.complete(second.correlationId(), reply);

        //then
        assertFalse(first.future().isDone());
        assertTrue(second.future().isDone());
        assertEquals(reply, second.future().get(1, TimeUnit.SECONDS));
    }
}
