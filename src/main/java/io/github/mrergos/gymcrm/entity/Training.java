package io.github.mrergos.gymcrm.entity;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Training {
    private Long id;
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private Long trainingType;
    private Date trainingDate;
    private Integer trainingDuration;
}
