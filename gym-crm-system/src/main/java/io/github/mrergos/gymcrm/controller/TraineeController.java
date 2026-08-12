package io.github.mrergos.gymcrm.controller;

import io.github.mrergos.gymcrm.dto.request.RegisterTraineeRequest;
import io.github.mrergos.gymcrm.dto.request.UpdateTraineeRequest;
import io.github.mrergos.gymcrm.dto.request.UpdateTraineeTrainersRequest;
import io.github.mrergos.gymcrm.dto.response.CredentialsResponse;
import io.github.mrergos.gymcrm.dto.response.TraineeResponse;
import io.github.mrergos.gymcrm.dto.response.TrainerShortResponse;
import io.github.mrergos.gymcrm.dto.response.TrainingTypeResponse;
import io.github.mrergos.gymcrm.dto.response.UpdateTraineeResponse;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.facade.GymFacade;
import io.github.mrergos.gymcrm.mapper.TraineeMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@Tag(name = "Trainees", description = "Trainee profile management")
public class TraineeController {

    private static final Logger log = LoggerFactory.getLogger(TraineeController.class);

    private final TraineeMapper traineeMapper;
    private final GymFacade facade;

    @Autowired
    public TraineeController(TraineeMapper traineeMapper, GymFacade facade) {
        this.traineeMapper = traineeMapper;
        this.facade = facade;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new trainee",
            description = "Creates a new Trainee profile and generates a unique username and password. " +
                    "Publicly accessible - does not require authentication. " +
                    "Fails if the caller's identity already exists as a Trainer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainee registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed (e.g. missing first/last name)", content = @Content)
    })
    public CredentialsResponse registerTrainee(@Valid @RequestBody RegisterTraineeRequest registerTraineeRequest) {
        log.info("Registering new trainee: firstName={}, lastName={}",
                registerTraineeRequest.firstName(), registerTraineeRequest.lastName());

        Trainee newTrainee = traineeMapper.toEntity(registerTraineeRequest);
        newTrainee = facade.createTraineeProfile(newTrainee);

        log.info("Trainee registered: username={}", newTrainee.getUsername());
        return new CredentialsResponse(newTrainee.getUsername(), newTrainee.getPassword());
    }

    @GetMapping("/{username}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get a trainee profile by username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee found"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content)
    })
    public TraineeResponse getTrainee(@Parameter(description = "Username of the trainee to fetch")
                                      @PathVariable("username") String username) {
        log.debug("Fetching trainee profile: username={}", username);

        Trainee trainee = facade.getTraineeProfile(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));

        return traineeMapper.toResponse(trainee);
    }

    @PutMapping("/{username}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a trainee profile",
            description = "Updates the trainee's personal details and active flag. Username cannot be changed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content)
    })
    public UpdateTraineeResponse updateTrainee(@PathVariable("username") String username,
                                               @Valid @RequestBody UpdateTraineeRequest updateTraineeRequest) {
        log.info("Updating trainee profile: username={}", username);

        Trainee trainee = traineeMapper.toEntity(updateTraineeRequest);
        trainee.setUsername(username);

        UpdateTraineeResponse response = traineeMapper.toUpdateResponse(facade.updateTraineeProfile(trainee));
        log.info("Trainee profile updated: username={}", username);
        return response;
    }

    @PatchMapping("/{username}/status")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Toggle trainee active status",
            description = "Flips the trainee's isActive flag (active -> inactive, inactive -> active). " +
                    "This is intentionally NOT idempotent: calling it twice in a row toggles the status twice. " +
                    "Use GET first if you need to know the resulting state, or read it off " +
                    "the response body of this call."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status toggled successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content)
    })
    public ResponseEntity<Void> toggleTraineeStatus(@PathVariable("username") String username) {
        log.info("Toggling active status for trainee: username={}", username);

        facade.toggleTraineeActive(username);

        log.info("Active status toggled for trainee: username={}", username);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/trainers")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Replace a trainee's assigned trainers",
            description = "Sets the trainee's full list of assigned trainers to exactly the given usernames " +
                    "(idempotent full-replace semantics)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainers list updated"),
            @ApiResponse(responseCode = "400", description = "One or more trainer usernames do not exist", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content)
    })
    public List<TrainerShortResponse> updateTraineeTrainers(@PathVariable("username") String username,
                                                            @Valid @RequestBody UpdateTraineeTrainersRequest body) {
        log.info("Updating trainers list for trainee: username={}, count={}", username, body.trainerUsernames().size());

        List<Trainer> trainers = facade.updateTraineeTrainers(username, body.trainerUsernames());

        return trainers.stream()
                .map(t -> new TrainerShortResponse(t.getUsername(), t.getFirstName(), t.getLastName(),
                        new TrainingTypeResponse(t.getSpecialization().getTrainingTypeName(), t.getSpecialization().getId())))
                .toList();
    }

    @GetMapping("/{username}/trainers/available")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "List trainers not yet assigned to a trainee",
            description = "Useful for building an 'add trainer' picker in a client UI."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned (may be empty). If there is no trainee, an empty list is returned."),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content)
    })
    public List<TrainerShortResponse> getAvailableTrainers(@PathVariable("username") String username) {
        log.debug("Fetching trainers not assigned to trainee: username={}", username);

        return facade.getTrainersNotAssigned(username).stream()
                .map(t -> new TrainerShortResponse(t.getUsername(), t.getFirstName(), t.getLastName(),
                        new TrainingTypeResponse(t.getSpecialization().getTrainingTypeName(), t.getSpecialization().getId())))
                .toList();
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete a trainee profile",
            description = "Hard-deletes the trainee and cascades deletion to their trainings (requirement #10). " +
                    "Idempotent in the HTTP sense: deleting an already-deleted trainee returns 404, not an error."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Trainee deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing access token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content)
    })
    public void deleteTrainee(@PathVariable("username") String username) {
        log.info("Deleting trainee profile: username={}", username);

        facade.deleteTraineeProfile(username);

        log.info("Trainee profile deleted: username={}", username);
    }
}
