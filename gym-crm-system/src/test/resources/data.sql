INSERT INTO training_types (training_type_name)
SELECT name FROM (
                     VALUES
                         ('Cardio'),
                         ('Fitness'),
                         ('Yoga'),
                         ('Zumba'),
                         ('Stretching'),
                         ('Resistance'),
                         ('CrossFit'),
                         ('Pilates')
                 ) AS v(name)
WHERE NOT EXISTS (
    SELECT 1 FROM training_types tt WHERE tt.training_type_name = v.name
);
