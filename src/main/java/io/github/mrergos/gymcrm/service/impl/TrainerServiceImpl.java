package io.github.mrergos.gymcrm.service.impl;

import io.github.mrergos.gymcrm.dao.TrainerDao;
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
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerServiceImpl implements TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }


    @Override
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
        log.info("Trainer profile created: id={}, username={}", saved.getUserId(), saved.getUsername());
        return saved;

    }

    @Override
    public Trainer updateTrainerProfile(Trainer trainer) {
        Assert.notNull(trainer.getUserId(), "Trainer id must not be null for update");

        trainerDao.findById(trainer.getUserId())
                .orElseThrow(() -> {
                    log.warn("Attempt to update non-existing trainer, id={}", trainer.getUserId());
                    return new EntityNotFoundException("Trainer not found with id: " + trainer.getUserId());
                });

        validateRequiredFields(trainer);

        log.info("Updating trainer profile, id={}", trainer.getUserId());
        return trainerDao.save(trainer);
    }

    private void validateRequiredFields(Trainer trainer) {
        Assert.hasText(trainer.getFirstName(), "First name must not be blank");
        Assert.hasText(trainer.getLastName(), "Last name must not be blank");
        Assert.hasText(trainer.getUsername(), "Username must not be blank");
        Assert.state(trainerDao.existsByUsername(trainer.getUsername()), "Username already exists");
        Assert.hasText(trainer.getPassword(), "Password must not be blank");
        Assert.state(trainer.getPassword().length() >= 10, "Password must be at least 10 characters");
        Assert.notNull(trainer.getSpecialization(), "Specialization must not be null");
    }

    @Override
    public Optional<Trainer> getTrainerProfile(Long id) {
        log.debug("Fetching trainer profile, id={}", id);
        return trainerDao.findById(id);
    }

    @Override
    public List<Trainer> getAllTrainers() {
        log.debug("Fetching all trainer profiles");
        return trainerDao.findAll();
    }
}
