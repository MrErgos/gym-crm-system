Feature: Training management
  As an authenticated client of the system
  I want to create trainings between a trainee and a trainer
  And view trainings filtered by date, trainer, or trainee
  So that training history stays accurate and accessible only to authorized callers

  Scenario: An authenticated user can create a training for an existing trainee and trainer
    Given a trainee is registered with first name "Nora" and last name "Owens"
    And a trainer is registered with first name "Peter", last name "Quist", specialization id 1
    And the trainee logs in with their generated credentials
    When the user creates a training named "Morning Strength Session" for trainee "Nora.Owens" and trainer "Peter.Quist" with date "2026-08-01" and duration 60
    Then the response status should be 201
    And the created training response should contain trainee "Nora.Owens", trainer "Peter.Quist" and training type "Cardio"

  Scenario: Creating a training for a non-existent trainee returns 404
    Given a trainer is registered with first name "Rosa", last name "Salazar", specialization id 1
    And the trainee logs in with their generated credentials
    When the user creates a training named "Evening Session" for a non-existent trainee "Nobody.Here" and trainer "Rosa.Salazar" with date "2026-08-02" and duration 45
    Then the response status should be 404

  Scenario: Creating a training for a non-existent trainer returns 404
    Given a trainee is registered with first name "Tara" and last name "Underwood"
    And the trainee logs in with their generated credentials
    When the user creates a training named "Evening Session" for trainee "Tara.Underwood" and a non-existent trainer "Nobody.Here" with date "2026-08-02" and duration 45
    Then the response status should be 404

  Scenario: Creating a training with a non-positive duration fails validation
    Given a trainee is registered with first name "Victor" and last name "Ward"
    And a trainer is registered with first name "Wendy", last name "Young", specialization id 1
    And the trainee logs in with their generated credentials
    When the user creates a training named "Bad Session" for trainee "Victor.Ward" and trainer "Wendy.Young" with date "2026-08-03" and a non-positive duration -10
    Then the response status should be 400

  Scenario: Creating a training without authentication is rejected
    Given a trainee is registered with first name "Xander" and last name "Zorn"
    And a trainer is registered with first name "Yara", last name "Adams", specialization id 1
    When the user creates a training named "Unauthorized Session" for trainee "Xander.Zorn" and trainer "Yara.Adams" with date "2026-08-04" and duration 30
    Then the response status should be 403


  Scenario: A trainee's trainings list contains a previously created training
    Given a trainee is registered with first name "Amir" and last name "Baxter"
    And a trainer is registered with first name "Bella", last name "Cross", specialization id 1
    And the trainee logs in with their generated credentials
    And a training named "Core Workout" is created for trainee "Amir.Baxter" and trainer "Bella.Cross" with date "2026-08-05" and duration 40
    When the user fetches trainings for trainee "Amir.Baxter"
    Then the training response list should contain training named "Core Workout"

  Scenario: Fetching trainings for a trainee with no trainings returns an empty list
    Given a trainee is registered with first name "Dana" and last name "Ellis"
    And the trainee logs in with their generated credentials
    When the user fetches trainings for trainee "Dana.Ellis"
    Then the training response list should be empty

  Scenario: A trainee's trainings can be filtered by date range and trainer name
    Given a trainee is registered with first name "Faye" and last name "Grant"
    And a trainer is registered with first name "Gus", last name "Hale", specialization id 1
    And the trainee logs in with their generated credentials
    And a training named "Leg Day" is created for trainee "Faye.Grant" and trainer "Gus.Hale" with date "2026-08-06" and duration 50
    When the user fetches trainings for trainee "Faye.Grant" with date range from "2026-08-01" to "2026-08-31", trainer "Gus Hale", type "Cardio"
    Then the training response list should contain training named "Leg Day"

  Scenario: Fetching trainings for a trainee without authentication is rejected
    Given a trainee is registered with first name "Ivy" and last name "Jensen"
    When the user fetches trainings for trainee "Ivy.Jensen"
    Then the response status should be 403


  Scenario: A trainer's trainings list contains a previously created training
    Given a trainee is registered with first name "Kian" and last name "Lang"
    And a trainer is registered with first name "Mira", last name "Novak", specialization id 1
    And the trainee logs in with their generated credentials
    And a training named "Cardio Blast" is created for trainee "Kian.Lang" and trainer "Mira.Novak" with date "2026-08-07" and duration 30
    When the user fetches trainings for trainer "Mira.Novak"
    Then the training response list should contain training named "Cardio Blast"

  Scenario: Fetching trainings for a trainer with no trainings returns an empty list
    Given a trainer is registered with first name "Omar", last name "Petit", specialization id 1
    And the trainee logs in with their generated credentials
    When the user fetches trainings for trainer "Omar.Petit"
    Then the training response list should be empty

  Scenario: Fetching trainings for a trainer without authentication is rejected
    Given a trainer is registered with first name "Quincy", last name "Rios", specialization id 1
    When the user fetches trainings for trainer "Quincy.Rios"
    Then the response status should be 403
