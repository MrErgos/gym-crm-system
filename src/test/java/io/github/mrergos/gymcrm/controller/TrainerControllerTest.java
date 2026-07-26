package io.github.mrergos.gymcrm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.github.mrergos.gymcrm.controller.TrainerController;
import io.github.mrergos.gymcrm.dto.request.RegisterTrainerRequest;
import io.github.mrergos.gymcrm.dto.request.UpdateTrainerRequest;
import io.github.mrergos.gymcrm.dto.response.CredentialsResponse;
import io.github.mrergos.gymcrm.dto.response.TrainerResponse;
import io.github.mrergos.gymcrm.dto.response.TrainingTypeResponse;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.facade.Credentials;
import io.github.mrergos.gymcrm.facade.GymFacade;
import io.github.mrergos.gymcrm.mapper.TrainerMapper;
import io.github.mrergos.gymcrm.security.BasicAuthCredentialsResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerController tests")
class TrainerControllerTest {

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private GymFacade facade;

    @Mock
    private BasicAuthCredentialsResolver credentialsResolver;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TrainerController controller;

    private final Credentials credentials = new Credentials("Anna.Lee", "password123");

    @Test
    @DisplayName("registerTrainer: delegates to facade and returns credentials")
    void registerTrainer_validRequest_shouldReturnCredentials() {
        //given
        RegisterTrainerRequest registerRequest = new RegisterTrainerRequest("Anna", "Lee", 1L);
        Trainer saved = new Trainer();
        saved.setUsername("Anna.Lee");
        saved.setPassword("generatedPass");

        when(facade.createTrainerProfile("Anna", "Lee", 1L)).thenReturn(saved);

        //when
        CredentialsResponse response = controller.registerTrainer(registerRequest);

        //then
        assertEquals("Anna.Lee", response.username());
        assertEquals("generatedPass", response.password());
    }

    @Test
    @DisplayName("getTrainer: resolves credentials and returns mapped trainer")
    void getTrainer_existingUsername_shouldReturnResponse() {
        //given
        Trainer trainer = new Trainer();
        trainer.setTrainees(Set.of());
        TrainerResponse mappedResponse = new TrainerResponse("Anna.Lee", "Anna", "Lee", true,
                new TrainingTypeResponse("Cardio", 1L), List.of());

        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTrainerProfile(credentials, "Anna.Lee")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(mappedResponse);

        //when
        TrainerResponse response = controller.getTrainer(request, "Anna.Lee");

        //then
        assertEquals(mappedResponse, response);
    }

    @Test
    @DisplayName("getTrainer: missing trainer throws EntityNotFoundException")
    void getTrainer_missingUsername_shouldThrow() {
        //given
        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTrainerProfile(credentials, "unknown")).thenReturn(Optional.empty());

        //when //then
        assertThrows(EntityNotFoundException.class, () -> controller.getTrainer(request, "unknown"));
    }

    @Test
    @DisplayName("updateTrainer: resolves specialization, updates and returns mapped response")
    void updateTrainer_validRequest_shouldReturnUpdatedResponse() {
        //given
        UpdateTrainerRequest updateRequest = new UpdateTrainerRequest("Anna", "Lee", 1L, true);
        TrainingType specialization = new TrainingType(1L, "Cardio");
        Trainer updated = new Trainer();
        TrainerResponse mappedResponse = new TrainerResponse("Anna.Lee", "Anna", "Lee", true,
                new TrainingTypeResponse("Cardio", 1L), List.of());

        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTrainingTypeById(1L)).thenReturn(Optional.of(specialization));
        when(facade.updateTrainerProfile(org.mockito.ArgumentMatchers.eq(credentials), org.mockito.ArgumentMatchers.any(Trainer.class)))
                .thenReturn(updated);
        when(trainerMapper.toResponse(updated)).thenReturn(mappedResponse);

        //when
        TrainerResponse response = controller.updateTrainer(request, "Anna.Lee", updateRequest);

        //then
        assertEquals(mappedResponse, response);
    }

    @Test
    @DisplayName("updateTrainer: unknown specialization throws EntityNotFoundException")
    void updateTrainer_unknownSpecialization_shouldThrow() {
        //given
        UpdateTrainerRequest updateRequest = new UpdateTrainerRequest("Anna", "Lee", 999L, true);

        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(facade.getTrainingTypeById(999L)).thenReturn(Optional.empty());

        //when //then
        assertThrows(EntityNotFoundException.class, () -> controller.updateTrainer(request, "Anna.Lee", updateRequest));
    }

    @Test
    @DisplayName("toggleTrainerStatus: resolves credentials and toggles via facade")
    void toggleTrainerStatus_shouldReturnOk() {
        //given
        when(credentialsResolver.resolve(request)).thenReturn(credentials);

        //when
        ResponseEntity<Void> response = controller.toggleTrainerStatus(request, "Anna.Lee");

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(facade).toggleTrainerActive(credentials, "Anna.Lee");
    }

    @Test
    @DisplayName("getAvailableTrainingTypes: maps types from facade")
    void getAvailableTrainingTypes_shouldReturnMappedList() {
        //given
        when(facade.getAvailableTrainingTypes()).thenReturn(List.of(new TrainingType(1L, "Cardio")));

        //when
        List<TrainingTypeResponse> response = controller.getAvailableTrainingTypes();

        //then
        assertEquals(1, response.size());
        assertEquals("Cardio", response.get(0).trainingTypeName());
        assertEquals(1L, response.get(0).id());
    }

    @Test
    @DisplayName("getAvailableTrainingTypes: returns empty list when none exist")
    void getAvailableTrainingTypes_noneExist_shouldReturnEmptyList() {
        //given
        when(facade.getAvailableTrainingTypes()).thenReturn(List.of());

        //when
        List<TrainingTypeResponse> response = controller.getAvailableTrainingTypes();

        //then
        assertEquals(0, response.size());
    }
}