package io.github.mrergos.workinghours.service;

import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.dto.response.TrainerWorkloadSummaryResponse;

public interface TrainerWorkloadService {

    void applyWorkload(TrainerWorkloadRequest request);

    TrainerWorkloadSummaryResponse getSummary(String username);
}
