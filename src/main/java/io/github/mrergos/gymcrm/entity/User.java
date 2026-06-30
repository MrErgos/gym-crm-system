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

    protected User(User other) {
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.username = other.username;
        this.password = other.password;
        this.isActive = other.isActive;
    }
}
