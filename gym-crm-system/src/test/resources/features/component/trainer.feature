Feature: Trainer profile management
  As a trainer or an authenticated client of the system
  I want to register, view, update a trainer profile
  And toggle their active status
  So that trainer data stays accurate and accessible only to authorized callers

  Scenario: A new trainer can be registered with a valid specialization
    When the user registers a trainer with first name "Diana", last name "Ross", specialization id 1
    Then the response status should be 201
    And the registration response should contain generated credentials

  Scenario: Registration fails when the first name is missing
    When the user registers a trainer with an empty first name and last name "Ross" and specialization id 1
    Then the response status should be 400

  Scenario: Registration fails when specialization id is missing
    When the user registers a trainer with first name "Diana", last name "Ross" and no specialization
    Then the response status should be 400


  Scenario: An authenticated user can fetch an existing trainer's profile
    Given a trainer is registered with first name "Carlos", last name "Mendez", specialization id 1
    And the trainee logs in with their generated credentials
    When the trainer fetches their own profile
    Then the trainer profile response should contain first name "Carlos" and last name "Mendez"

  Scenario: Fetching a non-existent trainer returns 404
    Given a trainer is registered with first name "Elena", last name "Kruger", specialization id 1
    And the trainee logs in with their generated credentials
    When the user fetches trainer profile for "Nobody.Here"
    Then the response status should be 404

  Scenario: Fetching a trainer profile without authentication is rejected
    Given a trainer is registered with first name "Felix", last name "Nowak", specialization id 1
    When the user fetches trainer profile for "Felix.Nowak"
    Then the response status should be 403


  Scenario: An authenticated user can update a trainer's profile
    Given a trainer is registered with first name "Greta", last name "Olsen", specialization id 1
    And the trainee logs in with their generated credentials
    When the trainer updates their profile with first name "Greta", last name "Olsen-Smith", specialization id 2, isActive true
    Then the response status should be 200
    And the trainer profile response should contain first name "Greta" and last name "Olsen-Smith"

  Scenario: Updating a trainer with a non-existent specialization id fails
    Given a trainer is registered with first name "Hugo", last name "Perez", specialization id 1
    And the trainee logs in with their generated credentials
    When the user updates profile for trainer "Hugo.Perez" with first name "Hugo", last name "Perez", specialization id 999999, isActive true
    Then the response status should be 404

  Scenario: Updating a non-existent trainer returns 404
    Given a trainer is registered with first name "Irene", last name "Quinn", specialization id 1
    And the trainee logs in with their generated credentials
    When the user updates profile for trainer "Nobody.Here" with first name "X", last name "Y", specialization id 1, isActive true
    Then the response status should be 404

  Scenario: Updating a trainer profile without authentication is rejected
    Given a trainer is registered with first name "Jack", last name "Reed", specialization id 1
    When the user updates profile for trainer "Jack.Reed" with first name "Jack", last name "Reed", specialization id 1, isActive true
    Then the response status should be 403


  Scenario: An authenticated user can toggle a trainer's active status
    Given a trainer is registered with first name "Karen", last name "Silva", specialization id 1
    And the trainee logs in with their generated credentials
    When the trainer fetches their own profile
    Then the trainer's active status should be true
    When the user toggles active status for trainer "Karen.Silva"
    And the trainer fetches their own profile
    Then the trainer's active status should be false

  Scenario: Toggling status for a non-existent trainer returns 404
    Given a trainer is registered with first name "Liam", last name "Turner", specialization id 1
    And the trainee logs in with their generated credentials
    When the user toggles active status for trainer "Nobody.Here"
    Then the response status should be 404


  Scenario: An authenticated user can fetch the list of available training types
    Given a trainer is registered with first name "Mona", last name "Vance", specialization id 1
    And the trainee logs in with their generated credentials
    When the user fetches available training types
    Then the response status should be 200
