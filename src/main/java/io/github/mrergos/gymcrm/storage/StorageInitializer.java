package io.github.mrergos.gymcrm.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
public class StorageInitializer {
    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    @Value("${storage.file.path:}")
    private String filePath;

    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Trainer> trainerStorage;
    private Map<Long, Training> trainingStorage;
    private ResourceLoader resourceLoader;

    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("traineeStorage")
    public void setTraineeStorage(Map<Long, Trainee> traineeStorage) {
        this.traineeStorage = traineeStorage;
    }

    @Autowired
    @Qualifier("trainerStorage")
    public void setTrainerStorage(Map<Long, Trainer> trainerStorage) {
        this.trainerStorage = trainerStorage;
    }

    @Autowired
    @Qualifier("trainingStorage")
    public void setTrainingStorage(Map<Long, Training> trainingStorage) {
        this.trainingStorage = trainingStorage;
    }

    @Autowired
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void loadStorage() {
        if (filePath == null || filePath.isBlank()) {
            log.warn("Filepath is blank. Storage will be empty.");
            return;
        }

        log.info("Loading storage from file: {}", filePath);

        Resource resource = resourceLoader.getResource(filePath);

        if (!resource.exists()) {
            log.warn("Storage file not found: {}. Storage will be empty.", filePath);
            return;
        }

        try (InputStream is = resource.getInputStream()) {
            if (!resource.isFile()) {
                log.warn("Filepath is pointing to directory: {}. Storage will be empty.", filePath);
                return;
            }
            StorageDTO data = objectMapper.readValue(is, StorageDTO.class);
            populateStorage(data);
        } catch (IOException e) {
            log.error("Failed to read storage file: {}", filePath, e);
        }
    }

    private void populateStorage(StorageDTO data) {
        data.getTrainees().forEach(trainee -> traineeStorage.put(trainee.getUserId(), trainee));
        data.getTrainers().forEach(trainer -> trainerStorage.put(trainer.getUserId(), trainer));
        data.getTrainings().forEach(training -> trainingStorage.put(training.getId(), training));

        log.info("Storage populated. Training: {}, trainees: {}, trainers: {}",
                trainingStorage.size(), traineeStorage.size(), trainerStorage.size());
    }
}
