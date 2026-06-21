package io.github.mrergos.gymcrm.entity;

import lombok.*;

@Getter
@Setter
@ToString
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
}
