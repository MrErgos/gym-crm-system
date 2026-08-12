package io.github.mrergos.gymcrm.controller;

import io.github.mrergos.gymcrm.dto.request.CreateTrainingRequest;
import io.github.mrergos.gymcrm.dto.response.TrainingResponse;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.facade.GymFacade;
import io.github.mrergos.gymcrm.mapper.TrainingMapper;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainings")
@Tag(name = "Trainings", description = "Training session management (create/read only)")
public class TrainingController {

    private static final Logger log = LoggerFactory.getLogger(TrainingController.class);

    private final GymFacade facade;
    private final TrainingMapper trainingMapper;

    public TrainingController(GymFacade facade, TrainingMapper trainingMapper) {
        this.facade = facade;
        this.trainingMapper = trainingMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create a training",
            description = "Creates a new training session between an existing trainee and trainer. " +
                    "The trainer is automatically added to the trainee's trainers list if not already assigned."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Training created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found", content = @Content)
    })
    public TrainingResponse createTraining(@Valid @RequestBody CreateTrainingRequest createRequest) {
        log.info("Creating training '{}' for trainee={}, trainer={}",
                createRequest.trainingName(), createRequest.traineeUsername(), createRequest.trainerUsername());

        Trainee trainee = facade.getTraineeProfile(createRequest.traineeUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainee not found: " + createRequest.traineeUsername()));

        Trainer trainer = facade.getTrainerProfile(createRequest.trainerUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainer not found: " + createRequest.trainerUsername()));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(createRequest.trainingName());
        training.setTrainingType(trainer.getSpecialization());
        training.setTrainingDate(createRequest.trainingDate());
        training.setTrainingDuration(createRequest.trainingDuration());

        Training saved = facade.createTraining(training);
        log.info("Training created: id={}", saved.getId());
        return trainingMapper.toResponse(saved);
    }

    @GetMapping("/trainee/{traineeUsername}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get a trainee's trainings",
            description = "Returns trainings for the given trainee, optionally filtered by date range, " +
                    "trainer full name, and training type name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned (may be empty)"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content)
    })
    public List<TrainingResponse> getTraineeTrainings(
            @Parameter(description = "Username of the trainee") @PathVariable("traineeUsername") String traineeUsername,
            @Parameter(description = "Filter: only trainings on/after this date")
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Filter: only trainings on/before this date")
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Filter: trainer's full name, e.g. 'John Smith'")
            @RequestParam(value = "trainerName", required = false) String trainerName,
            @Parameter(description = "Filter: training type name, e.g. 'Cardio'")
            @RequestParam(value = "trainingTypeName", required = false) String trainingTypeName) {

        log.debug("Fetching trainee trainings: username={}, from={}, to={}, trainer={}, type={}",
                traineeUsername, fromDate, toDate, trainerName, trainingTypeName);

        return facade.getTraineeTrainings(traineeUsername, fromDate, toDate, trainerName, trainingTypeName)
                .stream()
                .map(trainingMapper::toResponse)
                .toList();
    }

    @GetMapping("/trainer/{trainerUsername}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get a trainer's trainings",
            description = "Returns trainings for the given trainer, optionally filtered by date range and trainee full name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned (may be empty)"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content)
    })
    public List<TrainingResponse> getTrainerTrainings(
            @Parameter(description = "Username of the trainer") @PathVariable("trainerUsername") String trainerUsername,
            @Parameter(description = "Filter: only trainings on/after this date")
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Filter: only trainings on/before this date")
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Filter: trainee's full name, e.g. 'Jane Doe'")
            @RequestParam(value = "traineeName", required = false) String traineeName) {

        log.debug("Fetching trainer trainings: username={}, from={}, to={}, trainee={}",
                trainerUsername, fromDate, toDate, traineeName);

        return facade.getTrainerTrainings(trainerUsername, fromDate, toDate, traineeName)
                .stream()
                .map(trainingMapper::toResponse)
                .toList();
    }
}
