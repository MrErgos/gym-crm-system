package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.dao.TrainingTypeDao;
import io.github.mrergos.gymcrm.entity.Trainer;
import io.github.mrergos.gymcrm.entity.TrainingType;
import io.github.mrergos.gymcrm.exception.EntityNotFoundException;
import io.github.mrergos.gymcrm.service.TrainerService;
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
public class TrainerServiceImpl implements TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;
    private UsernameGenerator usernameGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Transactional
    public Trainer createTrainerProfile(String firstName, String lastName, Long specializationId) {
        Assert.hasText(firstName, "First name must not be blank");
        Assert.hasText(lastName, "Last name must not be blank");
        Assert.notNull(specializationId, "Specialization id must not be null");

        log.info("Fetching training type for id={}", specializationId);
        TrainingType specialization = trainingTypeDao.findById(specializationId)
                .orElseThrow(() -> new EntityNotFoundException("Training type not found with id: " + specializationId));
        return createTrainerProfile(firstName, lastName, specialization);
    }

    @Override
    @Transactional
    public Trainer createTrainerProfile(String firstName, String lastName, TrainingType specialization) {
        Assert.hasText(firstName, "First name must not be blank");
        Assert.hasText(lastName, "Last name must not be blank");
        Assert.notNull(specialization, "Specialization must not be null");

        log.info("Creating trainer profile for {} {}, specialization={}",
                firstName, lastName, specialization.getTrainingTypeName());

        String username = usernameGenerator.generate(firstName, lastName);
        String password = UserUtils.generatePassword();

        Trainer trainer = new Trainer();
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setActive(true);
        trainer.setSpecialization(specialization);

        Trainer saved = trainerDao.save(trainer);
        log.info("Trainer profile created: id={}, username={}", saved.getId(), saved.getUsername());
        return saved;

    }

    @Override
    @Transactional
    public Trainer updateTrainerProfile(Trainer trainer) {
        Assert.hasText(trainer.getUsername(), "Trainer username must not be blank for update");

        Trainer existing = trainerDao.findByUsername(trainer.getUsername())
                .orElseThrow(() -> {
                    log.warn("Attempt to update non-existing trainer, username={}", trainer.getUsername());
                    return new EntityNotFoundException("Trainer not found with username: " + trainer.getUsername());
                });

        applyChanges(trainer, existing);

        log.info("Updating trainer profile, username={}", trainer.getUsername());
        return trainerDao.save(existing);
    }

    private void applyChanges(Trainer trainer, Trainer existing) {
        existing.setFirstName(trainer.getFirstName());
        existing.setLastName(trainer.getLastName());
        existing.setSpecialization(trainer.getSpecialization());
        existing.setActive(trainer.isActive());
    }

    @Override
    @Transactional
    public void changePassword(String username, String newPassword) {
        Assert.hasText(newPassword, "Password must not be blank");
        Assert.isTrue(newPassword.length() >= 10, "Password must be at least 10 characters");

        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Attempt to change password for non-existing trainer, username={}", username);
                    return new EntityNotFoundException("Trainer not found with username: " + username);
                });

        trainer.setPassword(newPassword);
        trainerDao.save(trainer);
        log.info("Password changed for trainer, username={}", username);
    }

    @Override
    @Transactional
    public void toggleActive(String username) {
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Attempt to toggle active status for non-existing trainer, username={}", username);
                    return new EntityNotFoundException("Trainer not found with username: " + username);
                });

        boolean newStatus = !trainer.isActive();
        trainer.setActive(newStatus);
        trainerDao.save(trainer);
        log.info("Trainer active status toggled to {}, username={}", newStatus, username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainerProfile(String username) {
        log.debug("Fetching trainer profile, username={}", username);
        return trainerDao.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getAllTrainers() {
        log.debug("Fetching all trainer profiles");
        return trainerDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> getAvailableTrainingTypes() {
        log.debug("Fetching available training types");
        return trainingTypeDao.findAll();
    }

    @Override
    public Optional<TrainingType> getTrainingTypeById(Long id) {
        log.debug("Fetching training type by id={}", id);
        return trainingTypeDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        log.debug("Checking whether a trainer exists, username={}", username);
        return trainerDao.existsByUsername(username);
    }
}
