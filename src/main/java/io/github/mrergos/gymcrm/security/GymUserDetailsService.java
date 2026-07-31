package io.github.mrergos.gymcrm.security;

import io.github.mrergos.gymcrm.dao.TraineeDao;
import io.github.mrergos.gymcrm.dao.TrainerDao;
import io.github.mrergos.gymcrm.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GymUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(GymUserDetailsService.class);

    private static final String ROLE_TRAINEE = "ROLE_TRAINEE";
    private static final String ROLE_TRAINER = "ROLE_TRAINER";

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    public GymUserDetailsService(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username={}", username);

        Optional<? extends User> trainee = traineeDao.findByUsername(username);
        if (trainee.isPresent()) {
            return toUserDetails(trainee.get(), ROLE_TRAINEE);
        }

        Optional<? extends User> trainer = trainerDao.findByUsername(username);
        if (trainer.isPresent()) {
            return toUserDetails(trainer.get(), ROLE_TRAINER);
        }

        log.warn("User not found while loading user details, username={}", username);
        throw new UsernameNotFoundException("User not found: " + username);
    }

    private UserDetails toUserDetails(User user, String role) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .disabled(!user.isActive())
                .build();
    }
}
