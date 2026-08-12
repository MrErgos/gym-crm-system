package io.github.mrergos.gymcrm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    protected User(User other) {
        this.id = other.getId();
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.username = other.username;
        this.password = other.password;
        this.isActive = other.isActive;
    }
}
