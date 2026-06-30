package io.github.mrergos.gymcrm.storage;

import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class StorageDTO {
    private List<Trainee> trainees = new ArrayList<>();
    private List<Trainer> trainers = new ArrayList<>();
    private List<Training> trainings = new ArrayList<>();
}
