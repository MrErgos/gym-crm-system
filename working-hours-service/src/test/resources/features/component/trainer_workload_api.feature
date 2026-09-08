Feature: Trainer workload REST API
  As the Main Gym CRM microservice
  I want to submit trainer workload events and read back monthly summaries via REST
  So that trainers' accumulated training hours are correctly tracked

  Background:
    Given the working-hours-service is running with a clean database
    And I am authenticated as a trusted service caller

  Scenario: Submitting a workload event creates a new monthly summary
    Given trainer "John.Doe" has no recorded workload
    When I submit a workload event for trainer "John.Doe" with action "ADD", date "2026-08-01" and duration 60 minutes
    Then the workload event should be applied successfully
    And the workload summary for trainer "John.Doe" for year 2026 month 8 should show 60 minutes

  Scenario: Submitting multiple ADD events for the same month accumulates duration
    Given trainer "Anna.Lee" has no recorded workload
    When I submit a workload event for trainer "Anna.Lee" with action "ADD", date "2026-08-01" and duration 60 minutes
    And I submit a workload event for trainer "Anna.Lee" with action "ADD", date "2026-08-15" and duration 45 minutes
    Then the workload summary for trainer "Anna.Lee" for year 2026 month 8 should show 105 minutes

  Scenario: A DELETE event reduces the accumulated duration for the month
    Given trainer "Mike.Ross" has no recorded workload
    And I submit a workload event for trainer "Mike.Ross" with action "ADD", date "2026-08-01" and duration 90 minutes
    When I submit a workload event for trainer "Mike.Ross" with action "DELETE", date "2026-08-01" and duration 30 minutes
    Then the workload summary for trainer "Mike.Ross" for year 2026 month 8 should show 60 minutes

  Scenario: A DELETE event never drops the monthly total below zero
    Given trainer "Sara.Connor" has no recorded workload
    And I submit a workload event for trainer "Sara.Connor" with action "ADD", date "2026-08-01" and duration 20 minutes
    When I submit a workload event for trainer "Sara.Connor" with action "DELETE", date "2026-08-01" and duration 50 minutes
    Then the workload summary for trainer "Sara.Connor" for year 2026 month 8 should show 0 minutes

  Scenario: Requesting the summary for an unknown trainer returns 404
    Given trainer "Ghost.Trainer" has no recorded workload
    When I request the workload summary for trainer "Ghost.Trainer"
    Then the response status should be 404
    And the response error message should mention "Ghost.Trainer"

  Scenario: Submitting a workload event without a REST bearer token is rejected
    Given I am not authenticated
    When I submit a workload event for trainer "Anonymous.Attempt" with action "ADD", date "2026-08-01" and duration 30 minutes
    Then the response status should be 403

  Scenario: Submitting a workload event with a non-positive duration is rejected
    Given trainer "Bad.Duration" has no recorded workload
    When I submit a workload event for trainer "Bad.Duration" with action "ADD", date "2026-08-01" and duration -15 minutes
    Then the response status should be 400
