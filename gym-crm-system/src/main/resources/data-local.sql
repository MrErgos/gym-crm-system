DELETE FROM trainee_trainer;
DELETE FROM trainings;
DELETE FROM trainees;
DELETE FROM trainers;
DELETE FROM users;
DELETE FROM training_types;


INSERT INTO training_types (training_type_name) VALUES ('Fitness');
INSERT INTO training_types (training_type_name) VALUES ('Yoga');
INSERT INTO training_types (training_type_name) VALUES ('Zumba');
INSERT INTO training_types (training_type_name) VALUES ('Stretching');
INSERT INTO training_types (training_type_name) VALUES ('Resistance');
INSERT INTO training_types (training_type_name) VALUES ('CrossFit');
INSERT INTO training_types (training_type_name) VALUES ('Pilates');
INSERT INTO training_types (training_type_name) VALUES ('Cardio');


INSERT INTO users (firstName, lastName, username, password, is_active)
VALUES ('John', 'Doe', 'John.Doe', '$2a$10$v8rWNEQOt79BAHMfGKPehuRFjoUiDO/I8pvSCEA.Ejko.JLG/1Z82', true);

INSERT INTO trainees (user_id, date_of_birth, address)
VALUES ((SELECT id FROM users WHERE username = 'John.Doe'), '1995-05-15', '123 Maple Street, Springfield');


INSERT INTO users (firstName, lastName, username, password, is_active)
VALUES ('Emily', 'Watson', 'Emily.Watson', 'securepass2', true);

INSERT INTO trainees (user_id, date_of_birth, address)
VALUES ((SELECT id FROM users WHERE username = 'Emily.Watson'), '1998-11-23', '456 Oak Avenue, Riverdale');


INSERT INTO users (firstName, lastName, username, password, is_active)
VALUES ('Robert', 'Downey', 'Robert.Downey', 'securepass3', false);

INSERT INTO trainees (user_id, date_of_birth, address)
VALUES ((SELECT id FROM users WHERE username = 'Robert.Downey'), '1990-01-01', '789 Broadway, New York');


INSERT INTO users (firstName, lastName, username, password, is_active)
VALUES ('Alice', 'Cooper', 'Alice.Cooper', 'securepass4', true);

INSERT INTO trainees (user_id, date_of_birth, address)
VALUES ((SELECT id FROM users WHERE username = 'Alice.Cooper'), '2000-08-12', '221B Baker Street, London');



INSERT INTO users (firstName, lastName, username, password, is_active)
VALUES ('Jane', 'Smith', 'Jane.Smith', 'trainerpass1', true);

INSERT INTO trainers (user_id, specialization_id)
VALUES (
           (SELECT id FROM users WHERE username = 'Jane.Smith'),
           (SELECT id FROM training_types WHERE training_type_name = 'Fitness')
       );


INSERT INTO users (firstName, lastName, username, password, is_active)
VALUES ('David', 'Miller', 'David.Miller', 'trainerpass2', true);

INSERT INTO trainers (user_id, specialization_id)
VALUES (
           (SELECT id FROM users WHERE username = 'David.Miller'),
           (SELECT id FROM training_types WHERE training_type_name = 'Yoga')
       );


INSERT INTO users (firstName, lastName, username, password, is_active)
VALUES ('Michael', 'Johnson', 'Michael.Johnson', 'trainerpass3', true);

INSERT INTO trainers (user_id, specialization_id)
VALUES (
           (SELECT id FROM users WHERE username = 'Michael.Johnson'),
           (SELECT id FROM training_types WHERE training_type_name = 'Resistance')
       );


INSERT INTO users (firstName, lastName, username, password, is_active)
VALUES ('Sarah', 'Connor', 'Sarah.Connor', 'trainerpass4', false);

INSERT INTO trainers (user_id, specialization_id)
VALUES (
           (SELECT id FROM users WHERE username = 'Sarah.Connor'),
           (SELECT id FROM training_types WHERE training_type_name = 'Zumba')
       );


INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES
                                                         ((SELECT id FROM users WHERE username = 'John.Doe'), (SELECT id FROM users WHERE username = 'Jane.Smith')),
                                                         ((SELECT id FROM users WHERE username = 'John.Doe'), (SELECT id FROM users WHERE username = 'David.Miller')),
                                                         ((SELECT id FROM users WHERE username = 'Emily.Watson'), (SELECT id FROM users WHERE username = 'David.Miller')),
                                                         ((SELECT id FROM users WHERE username = 'Emily.Watson'), (SELECT id FROM users WHERE username = 'Michael.Johnson')),
                                                         ((SELECT id FROM users WHERE username = 'Alice.Cooper'), (SELECT id FROM users WHERE username = 'Jane.Smith'));


INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES
    (
        (SELECT id FROM users WHERE username = 'John.Doe'),
        (SELECT id FROM users WHERE username = 'Jane.Smith'),
        'Morning Strength Workout',
        (SELECT id FROM training_types WHERE training_type_name = 'Fitness'),
        '2026-07-20',
        60
    );

INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES
    (
        (SELECT id FROM users WHERE username = 'John.Doe'),
        (SELECT id FROM users WHERE username = 'David.Miller'),
        'Ashtanga Yoga Intro',
        (SELECT id FROM training_types WHERE training_type_name = 'Yoga'),
        '2026-07-21',
        90
    );

INSERT INTO trainings (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES
    (
        (SELECT id FROM users WHERE username = 'Emily.Watson'),
        (SELECT id FROM users WHERE username = 'David.Miller'),
        'Gentle Evening Yoga',
        (SELECT id FROM training_types WHERE training_type_name = 'Yoga'),
        '2026-07-22',
        75
    );