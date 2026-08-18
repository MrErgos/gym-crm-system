package io.github.mrergos.workinghours.messaging;

import io.github.mrergos.workinghours.dto.request.TrainerWorkloadRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WorkloadMessageValidator {

    public List<String> validate(TrainerWorkloadRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("Message payload is null or could not be parsed as TrainerWorkloadRequest");
            return errors;
        }
        if (isBlank(request.trainerUsername())) {
            errors.add("trainerUsername is required");
        }
        if (isBlank(request.trainerFirstName())) {
            errors.add("trainerFirstName is required");
        }
        if (isBlank(request.trainerLastName())) {
            errors.add("trainerLastName is required");
        }
        if (request.isActive() == null) {
            errors.add("isActive is required");
        }
        if (request.trainingDate() == null) {
            errors.add("trainingDate is required");
        }
        if (request.trainingDuration() == null) {
            errors.add("trainingDuration is required");
        } else if (request.trainingDuration() <= 0) {
            errors.add("trainingDuration must be a positive number of minutes");
        }
        if (request.actionType() == null) {
            errors.add("actionType is required");
        }

        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}