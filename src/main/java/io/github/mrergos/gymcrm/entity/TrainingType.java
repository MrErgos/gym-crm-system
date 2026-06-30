package io.github.mrergos.gymcrm.entity;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TrainingType {
    private String trainingTypeName;

    public TrainingType(TrainingType other) {
        this.trainingTypeName = other.trainingTypeName;
    }
}
