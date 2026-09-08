Feature: Trainer workload messaging
  As the working-hours-service
  I want to consume workload events from JMS and answer summary requests over JMS
  So that the system stays consistent even when integration happens asynchronously

  Background:
    Given the working-hours-service is running with a clean database

  Scenario: A valid workload event published on the queue is applied
    When a workload event message is published for trainer "Queue.Worker" with action "ADD", date "2026-08-01" and duration 60 minutes
    Then eventually the workload summary for trainer "Queue.Worker" for year 2026 month 8 should show 60 minutes

  Scenario: A summary request published on the request queue receives a reply with the data
    Given a workload event message is published for trainer "Reply.Trainer" with action "ADD", date "2026-08-01" and duration 60 minutes
    And eventually the workload summary for trainer "Reply.Trainer" for year 2026 month 8 should show 60 minutes
    When I send a JMS workload summary request for trainer "Reply.Trainer"
    Then the JMS reply should indicate the summary was found
    And the JMS reply summary for year 2026 month 8 should show 60 minutes

  Scenario: A summary request for an unknown trainer receives a not-found reply
    When I send a JMS workload summary request for trainer "Unknown.InMessaging"
    Then the JMS reply should indicate the summary was not found

  Scenario: An invalid workload event (missing trainer username) is routed to the dead-letter queue
    When an invalid workload event message is published with a missing trainer username
    Then the invalid message should be routed to the dead-letter queue
    And no workload summary should be created for the invalid event
