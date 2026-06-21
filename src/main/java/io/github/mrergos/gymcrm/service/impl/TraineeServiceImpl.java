package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.entity.Trainee;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.service.TraineeService;
import io.github.mrergos.gymcrm.service.UsernameGenerator;
import io.github.mrergos.gymcrm.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private TraineeDao traineeDao;
    private UsernameGenerator usernameGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Override
    public Trainee createTraineeProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        Assert.hasText(firstName, "First name must not be blank");
        Assert.hasText(lastName, "Last name must not be blank");

        log.info("Creating trainee profile for {} {}", firstName, lastName);

        String username = usernameGenerator.generate(firstName, lastName);

        String password = UserUtils.generatePassword();

        Trainee trainee = new Trainee();
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setPassword(password);
        trainee.setUsername(username);
        trainee.setActive(true);

        trainee = traineeDao.save(trainee);
        log.info("Trainee profile created, id={} username={}", trainee.getUserId(), trainee.getUsername());

        return trainee;
    }

    @Override
    public Trainee updateTraineeProfile(Trainee trainee) {
        Assert.notNull(trainee.getUserId(), "Trainee id must not be null for update");

        traineeDao.findById(trainee.getUserId())
                .orElseThrow(() -> {
                    log.warn("Attempt to update non-existent trainee, id={}", trainee.getUserId());
                    return new EntityNotFoundException("Trainee not found with id=" + trainee.getUserId());
                });

        validateRequiredFields(trainee);

        log.info("Updating trainee profile, id={}", trainee.getUserId());
        return traineeDao.save(trainee);

    }

    private void validateRequiredFields(Trainee trainee) {
        Assert.hasText(trainee.getFirstName(), "First name must not be blank");
        Assert.hasText(trainee.getLastName(), "Last name must not be blank");
        Assert.hasText(trainee.getUsername(), "Username must not be blank");
        Assert.state(traineeDao.existsByUsername(trainee.getUsername()), "Username already exists");
        Assert.hasText(trainee.getPassword(), "Password must not be blank");
        Assert.state(trainee.getPassword().length() >= 10, "Password must be at least 10 characters");
    }

    @Override
    public void deleteTraineeProfile(Long id) {
        Assert.notNull(id, "Trainee id must not be null");

        traineeDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attempt to delete non-existing trainee, id={}", id);
                    return new EntityNotFoundException("Trainee not found with id: " + id);
                });

        traineeDao.delete(id);
        log.info("Trainee profile deleted, id={}", id);
    }

    @Override
    public Optional<Trainee> getTraineeProfile(Long id) {
        log.debug("Fetching trainee profile, id={}", id);
        return traineeDao.findById(id);
    }

    @Override
    public List<Trainee> getAllTrainee() {
        log.debug("Fetching all trainee profiles");
        return traineeDao.findAll();
    }

}
