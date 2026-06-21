package io.github.mrergos.gymcrm.entity;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public abstract class User {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean isActive;
}
