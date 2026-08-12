package io.github.mrergos.gymcrm.integration;

import io.github.mrergos.gymcrm.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.gymcrm.integration.dto.TrainerWorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "working-hours-service")
public interface WorkingHoursClient {

    @PostMapping("/api/v1/trainers/workload")
    void submitWorkload(@RequestHeader("Authorization") String authorizationHeader,
                        @RequestHeader("X-Transaction-Id") String transactionId,
                        @RequestBody TrainerWorkloadRequest request);

    @GetMapping("/api/v1/trainers/{username}/workload")
    TrainerWorkloadSummaryResponse getWorkload(@RequestHeader("Authorization") String authorizationHeader,
                                               @RequestHeader("X-Transaction-Id") String transactionId,
                                               @PathVariable("username") String username);
}