package io.github.mrergos.gymcrm.mapper;

import io.github.mrergos.gymcrm.dto.request.RegisterTraineeRequest;
import io.github.mrergos.gymcrm.dto.request.UpdateTraineeRequest;
import io.github.mrergos.gymcrm.dto.response.TraineeResponse;
import io.github.mrergos.gymcrm.dto.response.TrainerShortResponse;
import io.github.mrergos.gymcrm.dto.response.TrainingTypeResponse;
import io.github.mrergos.gymcrm.dto.response.UpdateTraineeResponse;
import io.github.mrergos.gymcrm.entity.Trainee;
import org.springframework.stereotype.Component;

@Component
public class TraineeMapper {

    public Trainee toEntity(RegisterTraineeRequest request) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());

        return trainee;
    }

    public TraineeResponse toResponse(Trainee trainee) {
        return new TraineeResponse(
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.isActive(),
                trainee.getTrainers().stream()
                        .map(trainer -> new TrainerShortResponse(trainer.getUsername(),
                                trainer.getFirstName(),
                                trainer.getLastName(),
                                new TrainingTypeResponse(trainer.getSpecialization().getTrainingTypeName(), trainer.getSpecialization().getId())))
                        .toList()
        );
    }

    public UpdateTraineeResponse toUpdateResponse(Trainee trainee) {
        return new UpdateTraineeResponse(
                trainee.getUsername(),
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.isActive(),
                trainee.getTrainers().stream()
                        .map(trainer -> new TrainerShortResponse(trainer.getUsername(),
                                trainer.getFirstName(),
                                trainer.getLastName(),
                                new TrainingTypeResponse(trainer.getSpecialization().getTrainingTypeName(), trainer.getSpecialization().getId())))
                        .toList()
        );
    }

    public Trainee toEntity(UpdateTraineeRequest request) {
        Trainee trainee = new Trainee();
        trainee.setFirstName(request.firstName());
        trainee.setLastName(request.lastName());
        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
        trainee.setActive(request.isActive());

        return trainee;
    }
}
