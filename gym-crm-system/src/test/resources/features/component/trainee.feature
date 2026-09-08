Feature: Trainee profile management
  As a trainee or an authenticated client of the system
  I want to register, view, update, delete a trainee profile
  And manage the trainee's assigned trainers
  So that trainee data stays accurate and accessible only to authorized callers

  Scenario: A new trainee can be registered with full details
    When the user registers a trainee with first name "Alice", last name "Baker", date of birth "1995-04-12", address "221B Baker Street"
    Then the response status should be 201
    And the registration response should contain generated credentials

  Scenario: Registration fails when the first name is missing
    When the user registers a trainee with an empty first name and last name "Baker"
    Then the response status should be 400

  Scenario: An authenticated user can fetch an existing trainee's profile
    Given a trainee is registered with first name "Carla", last name "Diaz", date of birth "1990-01-01", address "Main St 1"
    And the trainee logs in with their generated credentials
    When the trainee fetches their own profile
    Then the trainee profile response should contain first name "Carla" and last name "Diaz"

  Scenario: Fetching a non-existent trainee returns 404
    Given a trainee is registered with first name "Ethan" and last name "Frost"
    And the trainee logs in with their generated credentials
    When the user fetches trainee profile for "Nobody.Here"
    Then the response status should be 404

  Scenario: Fetching a trainee profile without authentication is rejected
    Given a trainee is registered with first name "Grace" and last name "Hopper"
    When the user fetches trainee profile for "Grace.Hopper"
    Then the response status should be 403

  Scenario: An authenticated user can update a trainee's profile
    Given a trainee is registered with first name "Ivan" and last name "Jones"
    And the trainee logs in with their generated credentials
    When the trainee updates their profile with first name "Ivan", last name "Jones-Smith", date of birth "1992-06-15", address "New Address 5", isActive true
    Then the response status should be 200
    And the trainee profile response should contain first name "Ivan" and last name "Jones-Smith"

  Scenario: Updating a non-existent trainee returns 404
    Given a trainee is registered with first name "Karen" and last name "Lopez"
    And the trainee logs in with their generated credentials
    When the user updates profile for trainee "Nobody.Here" with first name "X", last name "Y", date of birth "1990-01-01", address "N/A", isActive true
    Then the response status should be 404

  Scenario: Updating a trainee profile without authentication is rejected
    Given a trainee is registered with first name "Mona" and last name "Nash"
    When the user updates profile for trainee "Mona.Nash" with first name "Mona", last name "Nash", date of birth "1990-01-01", address "N/A", isActive true
    Then the response status should be 403

  Scenario: An authenticated user can toggle a trainee's isActive status
    Given a trainee is registered with first name "Oscar" and last name "Pike"
    And the trainee logs in with their generated credentials
    When the trainee fetches their own profile
    Then the trainee's isActive status should be true
    When the user toggles isActive status for trainee "Oscar.Pike"
    And the trainee fetches their own profile
    Then the trainee's isActive status should be false

  Scenario: Toggling status for a non-existent trainee returns 404
    Given a trainee is registered with first name "Quinn" and last name "Reyes"
    And the trainee logs in with their generated credentials
    When the user toggles isActive status for trainee "Nobody.Here"
    Then the response status should be 404

  Scenario: A newly registered trainee has no assigned trainers and all trainers available
    Given a trainee is registered with first name "Sam" and last name "Tran"
    And the trainee logs in with their generated credentials
    When the user fetches unassigned trainers for trainee "Sam.Tran"
    Then the response status should be 200

  Scenario: Fetching unassigned trainers for a non-existent trainee returns an empty list
    Given a trainee is registered with first name "Uma" and last name "Voss"
    And the trainee logs in with their generated credentials
    When the user fetches unassigned trainers for trainee "Nobody.Here"
    Then the unassigned trainers list should be empty

  Scenario: An authenticated user can delete an existing trainee
    Given a trainee is registered with first name "Wendy" and last name "Xu"
    And the trainee logs in with their generated credentials
    When the trainee deletes their own profile
    Then the response status should be 204

  Scenario: Deleting an already-deleted trainee returns 404
    Given a trainee is registered with first name "Yusuf" and last name "Zane"
    And the trainee logs in with their generated credentials
    When the trainee deletes their own profile
    Then the response status should be 204
    When the trainee deletes profile for "Yusuf.Zane"
    Then the response status should be 403

  Scenario: Deleting a trainee profile without authentication is rejected
    Given a trainee is registered with first name "Zack" and last name "Adler"
    When the trainee deletes profile for "Zack.Adler"
    Then the response status should be 403
