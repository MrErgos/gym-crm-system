INSERT INTO training_types (training_type_name)
SELECT name FROM (
                     VALUES
                         ('Fitness'),
                         ('Yoga'),
                         ('Zumba'),
                         ('Stretching'),
                         ('Resistance'),
                         ('CrossFit'),
                         ('Pilates'),
                         ('Cardio')
                 ) AS v(name)
WHERE NOT EXISTS (
    SELECT 1 FROM training_types tt WHERE tt.training_type_name = v.name
);
