package io.github.mrergos.gymcrm.entity;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Trainer extends User {
    private TrainingType specialization;
    private Long userId;
}
