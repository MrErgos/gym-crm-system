package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.User;
import io.github.mrergos.gymcrm.exception.AuthenticationException;
import io.github.mrergos.gymcrm.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

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

    @Override
    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        Optional<Trainee> trainee = traineeDao.findByUsername(username);
        if (trainee.isPresent()) {
            checkPassword(trainee.get(), password);
            return;
        }

        Optional<Trainer> trainer = trainerDao.findByUsername(username);
        if (trainer.isPresent()) {
            checkPassword(trainer.get(), password);
            return;
        }

        log.warn("Authentication failed: user not found, username={}", username);
        throw new AuthenticationException("Invalid username or password");
    }

    private void checkPassword(User user, String password) {
        if (!user.getPassword().equals(password)) {
            log.warn("Authentication failed: password does not match, username={}", user.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }
        log.debug("Authentication successful, username={}", user.getUsername());
    }
}
