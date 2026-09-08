package io.github.mrergos.workinghours.messaging.dto;

import io.github.mrergos.workinghours.dto.response.TrainerWorkloadSummaryResponse;

public record WorkloadSummaryReply(
        boolean found,
        String errorMessage,
        TrainerWorkloadSummaryResponse summary
) {

    public static WorkloadSummaryReply of(TrainerWorkloadSummaryResponse summary) {
        return new WorkloadSummaryReply(true, null, summary);
    }

    public static WorkloadSummaryReply notFound(String errorMessage) {
        return new WorkloadSummaryReply(false, errorMessage, null);
    }
}