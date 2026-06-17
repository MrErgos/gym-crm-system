package io.github.mrergos.gymcrm.entity;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Trainee extends User {
    private Date dateOfBirth;
    private String address;
}
