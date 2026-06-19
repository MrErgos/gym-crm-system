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
    private Long specializationId;
    private Long userId;
}
