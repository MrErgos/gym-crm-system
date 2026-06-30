package io.github.mrergos.gymcrm.entity;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Trainee extends User {
    private LocalDate dateOfBirth;
    private String address;
    private Long userId;

    public Trainee(String firstName, String lastName, String username, String password,
                   boolean isActive,
                   LocalDate dateOfBirth, String address, Long userId) {
        super(firstName, lastName, username, password, isActive);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.userId = userId;
    }

    public Trainee(Trainee other) {
        super(other);
        this.dateOfBirth = other.dateOfBirth;
        this.address = other.address;
        this.userId = other.userId;
    }
}
