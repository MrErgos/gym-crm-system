package io.github.mrergos.workinghours.controller;

import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import io.github.mrergos.workinghours.dto.response.TrainerWorkloadSummaryResponse;
import io.github.mrergos.workinghours.service.TrainerWorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainers")
@Tag(name = "Trainer Workload", description = "Accepts trainer workload events (planned/cancelled trainings) and exposes monthly training-hours summaries")
@SecurityRequirement(name = "bearerAuth")
public class TrainerWorkloadController {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadController.class);

    private final TrainerWorkloadService trainerWorkloadService;

    public TrainerWorkloadController(TrainerWorkloadService trainerWorkloadService) {
        this.trainerWorkloadService = trainerWorkloadService;
    }

    @PostMapping("/workload")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Submit a trainer workload event",
            description = "Called by the Main Gym CRM Microservice every time a training session is planned (ADD) " +
                    "or cancelled (DELETE) for a trainer. Updates the trainer's monthly summary of training minutes " +
                    "held in the in-memory database. Intended for service-to-service use only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workload event applied successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid service token", content = @Content)
    })
    public void submitWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        log.info("Received workload event: trainer={}, action={}, date={}, duration={}min",
                request.trainerUsername(), request.actionType(), request.trainingDate(), request.trainingDuration());

        trainerWorkloadService.applyWorkload(request);

        log.info("Workload event applied successfully for trainer={}", request.trainerUsername());
    }

    @GetMapping("/{username}/workload")
    @Operation(
            summary = "Get a trainer's monthly training-hours summary",
            description = "Returns the trainer's full training-hours summary, grouped by year and month, " +
                    "as accumulated from previously submitted workload events."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid service token", content = @Content),
            @ApiResponse(responseCode = "404", description = "No workload data found for this trainer", content = @Content)
    })
    public TrainerWorkloadSummaryResponse getWorkloadSummary(
            @Parameter(description = "Username of the trainer") @PathVariable("username") String username) {

        log.debug("Fetching workload summary for trainer={}", username);

        TrainerWorkloadSummaryResponse response = trainerWorkloadService.getSummary(username);

        log.debug("Workload summary returned for trainer={}, years={}", username, response.years().size());
        return response;
    }
}
