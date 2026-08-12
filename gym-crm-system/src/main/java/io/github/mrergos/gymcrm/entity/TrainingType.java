package io.github.mrergos.gymcrm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "training_types")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true)
    private String trainingTypeName;

    public TrainingType(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    public TrainingType(TrainingType other) {
        this.id = other.id;
        this.trainingTypeName = other.trainingTypeName;
    }
}
