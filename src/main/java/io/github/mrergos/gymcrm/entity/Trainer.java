package io.github.mrergos.gymcrm.entity;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class Trainer extends User {
    private TrainingType specialization;
    private Long userId;

    public Trainer(String firstname, String lastname, String username, String password, boolean isActive,
                   TrainingType specialization, Long userId) {
        super(firstname, lastname, username, password, isActive);
        this.specialization = specialization;
        this.userId = userId;
    }
}
