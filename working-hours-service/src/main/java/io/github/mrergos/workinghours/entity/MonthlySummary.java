package io.github.mrergos.workinghours.entity;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummary {

    private int month;
    private int totalDurationMinutes;
}
