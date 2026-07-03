package io.github.mrergos.gymcrm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainees")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Trainee extends User {

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String address;

    @OneToMany(mappedBy = "trainee", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Training> trainings = new ArrayList<>();

    public Trainee(String firstName, String lastName, String username, String password,
                   boolean isActive,
                   LocalDate dateOfBirth, String address, Long userId) {
        super(userId, firstName, lastName, username, password, isActive);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public Trainee(Trainee other) {
        super(other);
        this.dateOfBirth = other.dateOfBirth;
        this.address = other.address;
    }
}
