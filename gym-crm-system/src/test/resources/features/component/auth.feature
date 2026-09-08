Feature: Authentication
  As a registered trainee or trainer
  I want to log in, log out, and change my password
  So that I can securely access my profile and protected endpoints

  Scenario: A newly registered trainee can log in with their generated credentials
    Given a trainee is registered with first name "John" and last name "Carter"
    When the trainee logs in with their generated credentials
    Then the login response status should be 200
    And a valid access token should be returned

  Scenario: Login fails with an invalid password
    Given a trainee is registered with first name "Mia" and last name "Wallace"
    When the trainee logs in with username "Mia.Wallace" and password "WrongPassword123"
    Then the login response status should be 401

  Scenario: Login fails with a non-existent username
    When the trainee logs in with username "Ghost.User" and password "DoesNotMatter1"
    Then the login response status should be 401

  Scenario: Account is temporarily locked after repeated failed login attempts
    Given a trainee is registered with first name "Vincent" and last name "Vega"
    When the trainee attempts to log in with username "Vincent.Vega" and an invalid password 3 times
    Then the login response status should be 401

  Scenario: A logged-in user can change their password and log in with the new one
    Given a trainee is registered with first name "Jules" and last name "Winnfield"
    And the trainee logs in with their generated credentials
    When the trainee changes their password to "NewSecurePass99"
    Then the change password response status should be 200
    And the trainee can log in with username "Jules.Winnfield" and password "NewSecurePass99"

  Scenario: A logged-out access token can no longer be used
    Given a trainee is registered with first name "Marsellus" and last name "Wallace"
    And the trainee logs in with their generated credentials
    When the trainee logs out
    Then the logout response status should be 200
    And using the revoked token to fetch the trainee's own profile returns 403

  Scenario: Accessing a protected endpoint without a token is rejected
    When an unauthenticated request is made to fetch trainee "Anyone" profile
    Then the response status should be 403
