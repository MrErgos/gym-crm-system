package io.github.mrergos.gymcrm.entity;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Training {
    private Long id;
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private Integer trainingDuration;

    public Training(Training other) {
        this.id = other.id;
        this.traineeId = other.traineeId;
        this.trainerId = other.trainerId;
        this.trainingName = other.trainingName;
        this.trainingType = other.trainingType != null ? new TrainingType(other.trainingType) : null;
        this.trainingDate = other.trainingDate;
        this.trainingDuration = other.trainingDuration;
    }

}
