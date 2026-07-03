package io.github.mrergos.gymcrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainers")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Trainer extends User {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private TrainingType specialization;

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Training> trainings = new ArrayList<>();


    public Trainer(String firstName, String lastName, String username, String password,
                   boolean isActive, TrainingType specialization, Long userId) {
        super(userId, firstName, lastName, username, password, isActive);
        this.specialization = specialization;
    }

    public Trainer(Trainer other) {
        super(other);
        this.specialization = other.specialization != null ? new TrainingType(other.specialization) : null;
    }
}
