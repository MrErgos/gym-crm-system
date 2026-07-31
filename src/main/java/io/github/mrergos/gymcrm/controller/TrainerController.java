package io.github.mrergos.gymcrm.controller;

import io.github.mrergos.gymcrm.dto.request.RegisterTrainerRequest;
import io.github.mrergos.gymcrm.dto.request.UpdateTrainerRequest;
import io.github.mrergos.gymcrm.dto.response.CredentialsResponse;
import io.github.mrergos.gymcrm.dto.response.TrainerResponse;
import io.github.mrergos.gymcrm.dto.response.TrainingTypeResponse;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.facade.GymFacade;
import io.github.mrergos.gymcrm.mapper.TrainerMapper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainers")
@Tag(name = "Trainers", description = "Trainer profile management")
public class TrainerController {

    private static final Logger log = LoggerFactory.getLogger(TrainerController.class);

    private final TrainerMapper trainerMapper;
    private final GymFacade facade;

    public TrainerController(TrainerMapper trainerMapper, GymFacade facade) {
        this.trainerMapper = trainerMapper;
        this.facade = facade;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new trainer",
            description = "Creates a new Trainer profile and generates a unique username and password. " +
                    "Publicly accessible - does not require authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainer registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed, or specialization id does not exist", content = @Content)
    })
    public CredentialsResponse registerTrainer(@Valid @RequestBody RegisterTrainerRequest request) {
        log.info("Registering new trainer: firstName={}, lastName={}, specializationId={}",
                request.firstName(), request.lastName(), request.specializationId());

        Trainer newTrainer = facade.createTrainerProfile(request.firstName(), request.lastName(), request.specializationId());

        log.info("Trainer registered: username={}", newTrainer.getUsername());
        return new CredentialsResponse(newTrainer.getUsername(), newTrainer.getPassword());
    }

    @GetMapping("/{username}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get a trainer profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainer found"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public TrainerResponse getTrainer(@Parameter(description = "Username of the trainer to fetch")
                                      @PathVariable("username") String username) {
        log.debug("Fetching trainer profile: username={}", username);

        Trainer trainer = facade.getTrainerProfile(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));

        return trainerMapper.toResponse(trainer);
    }

    @PutMapping("/{username}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a trainer profile",
            description = "Updates the trainer's personal details, specialization and active flag. " +
                    "Username cannot be changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainer updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed, or specialization id does not exist", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public TrainerResponse updateTrainer(@PathVariable("username") String username,
                                         @Valid @RequestBody UpdateTrainerRequest updateRequest) {
        log.info("Updating trainer profile: username={}", username);

        TrainingType specialization = facade.getTrainingTypeById(updateRequest.specializationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found with id: " + updateRequest.specializationId()));

        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        trainer.setFirstName(updateRequest.firstName());
        trainer.setLastName(updateRequest.lastName());
        trainer.setSpecialization(specialization);
        trainer.setActive(updateRequest.isActive());

        TrainerResponse response = trainerMapper.toResponse(facade.updateTrainerProfile(trainer));
        log.info("Trainer profile updated: username={}", username);
        return response;
    }

    @PatchMapping("/{username}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Toggle trainer active status",
            description = "Flips the trainer's isActive flag (active -> inactive, inactive -> active). " +
                    "This is intentionally NOT idempotent: calling it twice in a row toggles the status twice."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status toggled successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content)
    })
    public ResponseEntity<Void> toggleTrainerStatus(@PathVariable("username") String username) {
        log.info("Toggling active status for trainer: username={}", username);

        facade.toggleTrainerActive(username);

        log.info("Active status toggled for trainer: username={}", username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/training-types")
    @Operation(
            summary = "List available training types",
            description = "Returns the constant list of training types. " +
                    "Useful for populating a specialization picker during trainer registration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned")
    })
    public List<TrainingTypeResponse> getAvailableTrainingTypes() {
        log.debug("Fetching available training types");

        return facade.getAvailableTrainingTypes().stream()
                .map(t -> new TrainingTypeResponse(t.getTrainingTypeName(), t.getId()))
                .toList();
    }
}
