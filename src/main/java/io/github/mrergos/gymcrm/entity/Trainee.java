package io.github.mrergos.gymcrm.entity;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Trainee extends User {
    private LocalDate dateOfBirth;
    private String address;
    private Long userId;
}
