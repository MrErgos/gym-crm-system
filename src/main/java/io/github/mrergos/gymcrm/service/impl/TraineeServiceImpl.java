package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.metrics.GymMetrics;
import io.github.mrergos.gymcrm.service.TraineeService;
import io.github.mrergos.gymcrm.service.UsernameGenerator;
import io.github.mrergos.gymcrm.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private GymMetrics gymMetrics;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setGymMetrics(GymMetrics gymMetrics) {
        this.gymMetrics = gymMetrics;
    }

    @Override
    @Transactional
    public Trainee createTraineeProfile(Trainee trainee) {
        Assert.hasText(trainee.getFirstName(), "First name must not be blank");
        Assert.hasText(trainee.getLastName(), "Last name must not be blank");

        log.info("Creating trainee profile for {} {}", trainee.getFirstName(), trainee.getLastName());

        String username = usernameGenerator.generate(trainee.getFirstName(), trainee.getLastName());

        String password = UserUtils.generatePassword();

        trainee.setPassword(password);
        trainee.setUsername(username);
        trainee.setActive(true);

        trainee = traineeDao.save(trainee);
        gymMetrics.incrementTraineeRegistrations();
        log.info("Trainee profile created, id={} username={}", trainee.getId(), trainee.getUsername());

        return trainee;
    }

    @Override
    @Transactional
    public Trainee updateTraineeProfile(Trainee trainee) {
        Assert.notNull(trainee.getUsername(), "Trainee username must not be null for update");

        Trainee existing = traineeDao.findByUsername(trainee.getUsername())
                .orElseThrow(() -> {
                    log.warn("Attempt to update non-existent trainee, username={}", trainee.getUsername());
                    return new EntityNotFoundException("Trainee not found with username=" + trainee.getUsername());
                });

        applyChanges(trainee, existing);

        log.info("Updating trainee profile, username={}", trainee.getUsername());
        return traineeDao.save(existing);

    }

    private void applyChanges(Trainee trainee, Trainee existing) {
        existing.setFirstName(trainee.getFirstName());
        existing.setLastName(trainee.getLastName());
        existing.setDateOfBirth(trainee.getDateOfBirth() != null ? trainee.getDateOfBirth() : existing.getDateOfBirth());
        existing.setAddress(trainee.getAddress() != null ? trainee.getAddress() : existing.getAddress());
        existing.setActive(trainee.isActive());
    }

    @Override
    @Transactional
    public void changePassword(String username, String newPassword) {
        Assert.hasText(newPassword, "Password must not be blank");
        Assert.isTrue(newPassword.length() >= 10, "Password must be at least 10 characters");

        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Attempt to change password for non-existing trainee, username={}", username);
                    return new EntityNotFoundException("Trainee not found with username: " + username);
                });

        trainee.setPassword(newPassword);
        traineeDao.save(trainee);
        log.info("Password changed for trainee, username={}", username);
    }

    @Override
    @Transactional
    public void toggleActive(String username) {
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Attempt to toggle active status for non-existing trainee, username={}", username);
                    return new EntityNotFoundException("Trainee not found with username: " + username);
                });

        boolean newStatus = !trainee.isActive();
        trainee.setActive(newStatus);
        traineeDao.save(trainee);
        log.info("Trainee active status toggled to {}, username={}", newStatus, username);
    }

    @Override
    @Transactional
    public void deleteTraineeProfile(String username) {
        Assert.notNull(username, "Trainee username must not be null");

        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Attempt to delete non-existing trainee, username={}", username);
                    return new EntityNotFoundException("Trainee not found with username: " + username);
                });

        traineeDao.delete(trainee);
        log.info("Trainee profile deleted, username={}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeProfile(String username) {
        log.debug("Fetching trainee profile, username={}", username);
        return traineeDao.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> getAllTrainees() {
        log.debug("Fetching all trainee profiles");
        return traineeDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getTrainersNotAssigned(String traineeUsername) {
        log.debug("Fetching trainers not assigned to trainee, username={}", traineeUsername);
        return trainerDao.findAllNotAssignedToTrainee(traineeUsername);
    }

    @Override
    @Transactional
    public List<Trainer> updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        Assert.notNull(trainerUsernames, "Trainer usernames list must not be null");

        log.info("Updating trainers list for trainee, username={}, trainers={}", traineeUsername, trainerUsernames);
        return traineeDao.updateTrainers(traineeUsername, trainerUsernames);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        log.debug("Checking whether a trainee exists, username={}", username);
        return traineeDao.existsByUsername(username);
    }
}
