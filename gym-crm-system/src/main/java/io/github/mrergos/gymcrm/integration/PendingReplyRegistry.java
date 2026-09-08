package io.github.mrergos.gymcrm.integration;

import io.github.mrergos.gymcrm.integration.dto.WorkloadSummaryReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PendingReplyRegistry {

    private static final Logger log = LoggerFactory.getLogger(PendingReplyRegistry.class);

    private final ConcurrentHashMap<String, CompletableFuture<WorkloadSummaryReply>> pendingReplies =
            new ConcurrentHashMap<>();

    public Registration register() {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<WorkloadSummaryReply> future = new CompletableFuture<>();
        pendingReplies.put(correlationId, future);
        return new Registration(correlationId, future);
    }

    public void complete(String correlationId, WorkloadSummaryReply reply) {
        if (correlationId == null) {
            log.warn("Received workload summary reply without a correlation id, ignoring");
            return;
        }
        CompletableFuture<WorkloadSummaryReply> future = pendingReplies.get(correlationId);
        if (future == null) {
            log.warn("Received workload summary reply with no matching pending request, correlationId={}",
                    correlationId);
            return;
        }
        future.complete(reply);
    }

    public void remove(String correlationId) {
        pendingReplies.remove(correlationId);
    }

    public record Registration(String correlationId, CompletableFuture<WorkloadSummaryReply> future) {
    }
}