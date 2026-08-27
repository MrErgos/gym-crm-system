package io.github.mrergos.workinghours.repository;

import io.github.mrergos.workinghours.entity.TrainerWorkloadSummaryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TrainerWorkloadSummaryRepository extends MongoRepository<TrainerWorkloadSummaryDocument, String> {
    List<TrainerWorkloadSummaryDocument> findByTrainerFirstNameAndTrainerLastName(String firstName, String lastName);
}
