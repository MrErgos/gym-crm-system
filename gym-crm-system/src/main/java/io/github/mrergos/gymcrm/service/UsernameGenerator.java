package io.github.mrergos.gymcrm.service;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsernameGenerator {
    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    public String generate(String firstName, String lastName) {
        String username = UserUtils.generateUniqueUsername(firstName,
                lastName,
                this::checkUsernameExists);

        log.debug("Generated username '{}' for {} {}", username, firstName, lastName);
        return username;
    }

    public boolean checkUsernameExists(String username) {
        return traineeDao.existsByUsername(username) || trainerDao.existsByUsername(username);
    }
}
