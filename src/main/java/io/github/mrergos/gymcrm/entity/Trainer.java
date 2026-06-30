package io.github.mrergos.gymcrm.entity;

import lombok.*;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Trainer extends User {
    private TrainingType specialization;
    private Long userId;

    public Trainer(String firstName, String lastName, String username, String password,
                   boolean isActive, TrainingType specialization, Long userId) {
        super(firstName, lastName, username, password, isActive);
        this.specialization = specialization;
        this.userId = userId;
    }

    public Trainer(Trainer other) {
        super(other);
        this.specialization = other.specialization != null ? new TrainingType(other.specialization) : null;
        this.userId = other.userId;
    }
}
