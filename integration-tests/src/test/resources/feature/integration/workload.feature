Feature: Cross-service workload synchronization between Gym CRM System and Working Hours Service

  Background:
    Given both microservices are running and connected via Artemis broker

  Scenario: Creating a training in Gym CRM System asynchronously updates trainer workload in Working Hours Service
    Given a registered active trainer with username "sync.trainer" and specialization "Fitness"
    And a registered active trainee with username "sync.trainee"
    And trainer "sync.trainer" has no workload data in Working Hours Service yet
    When the user creates a training named "Cross-Service Workout" for trainee "sync.trainee" and trainer "sync.trainer" with date "2026-09-15", duration 60, type "Fitness"
    Then the creation response status should be 200 or 201
    And eventually trainer "sync.trainer" should have 60 minutes recorded for year 2026 month 9 in Working Hours Service

  Scenario: Creating a second training for the same trainer and month accumulates workload minutes
    Given a registered active trainer with username "accrue.trainer" and specialization "Fitness"
    And a registered active trainee with username "accrue.trainee"
    When the user creates a training named "Morning Session" for trainee "accrue.trainee" and trainer "accrue.trainer" with date "2026-09-05", duration 45, type "Fitness"
    And the creation response status should be 200 or 201
    And eventually trainer "accrue.trainer" should have 45 minutes recorded for year 2026 month 9 in Working Hours Service
    When the user creates a training named "Evening Session" for trainee "accrue.trainee" and trainer "accrue.trainer" with date "2026-09-20", duration 30, type "Fitness"
    Then the creation response status should be 200 or 201
    And eventually trainer "accrue.trainer" should have 75 minutes recorded for year 2026 month 9 in Working Hours Service

  Scenario: Creating a training while Working Hours Service is down still succeeds, and the workload update is delivered once it comes back
    Given a registered active trainer with username "resilient.trainer" and specialization "Fitness"
    And a registered active trainee with username "resilient.trainee"
    And Working Hours Service is stopped
    When the user creates a training named "Outage Workout" for trainee "resilient.trainee" and trainer "resilient.trainer" with date "2026-09-16", duration 50, type "Fitness"
    Then the creation response status should be 200 or 201
    When Working Hours Service is started again
    Then eventually trainer "resilient.trainer" should have 50 minutes recorded for year 2026 month 9 in Working Hours Service
