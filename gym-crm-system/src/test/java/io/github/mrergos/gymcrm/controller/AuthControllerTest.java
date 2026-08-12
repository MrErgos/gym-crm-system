package io.github.mrergos.gymcrm.controller;

import io.github.mrergos.gymcrm.dto.request.UpdateCredentialsRequest;
import io.github.mrergos.gymcrm.dto.response.TokenResponse;
import io.github.mrergos.gymcrm.exception.AccountLockedException;
import io.github.mrergos.gymcrm.exception.AuthenticationException;
import io.github.mrergos.gymcrm.facade.AuthenticationFacade;
import io.github.mrergos.gymcrm.facade.Credentials;
import io.github.mrergos.gymcrm.facade.GymFacade;
import io.github.mrergos.gymcrm.metrics.GymMetrics;
import io.github.mrergos.gymcrm.security.BasicAuthCredentialsResolver;
import io.github.mrergos.gymcrm.security.SecurityUtils;
import io.github.mrergos.gymcrm.security.bruteforce.LoginAttemptService;
import io.github.mrergos.gymcrm.security.jwt.JwtService;
import io.github.mrergos.gymcrm.security.jwt.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController tests")
class AuthControllerTest {

    @Mock
    private GymFacade gymFacade;

    @Mock
    private BasicAuthCredentialsResolver credentialsResolver;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController controller;

    @Test
    @DisplayName("login: resolves credentials, authenticates and returns token")
    void login_validCredentials_shouldReturnToken() {
        //given
        Credentials credentials = new Credentials("John.Doe", "password123");
        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(loginAttemptService.isBlocked("John.Doe")).thenReturn(false);
        when(gymMetrics.recordAuthenticationTime(any(java.util.function.Supplier.class)))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<?> supplier = invocation.getArgument(0);
                    return supplier.get();
                });
        when(jwtService.generateToken("John.Doe")).thenReturn("generated-jwt-token");

        //when
        TokenResponse response = controller.login(request);

        //then
        assertEquals("generated-jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        verify(loginAttemptService).onSuccessfulLogin("John.Doe");
        verify(gymMetrics).recordAuthenticationSuccess();
    }

    @Test
    @DisplayName("login: locked account throws AccountLockedException")
    void login_lockedAccount_shouldThrow() {
        //given
        Credentials credentials = new Credentials("John.Doe", "password123");
        when(credentialsResolver.resolve(request)).thenReturn(credentials);
        when(loginAttemptService.isBlocked("John.Doe")).thenReturn(true);

        //when
        //then
        assertThrows(AccountLockedException.class, () -> controller.login(request));
    }

    @Test
    @DisplayName("logout: revokes token extracted from Authorization header")
    void logout_validToken_shouldReturnOk() {
        //given
        String authorizationHeader = "Bearer some.jwt.token";
        when(jwtService.extractTokenId("some.jwt.token")).thenReturn("jti-123");
        when(jwtService.extractExpiration("some.jwt.token")).thenReturn(new Date());

        //when
        ResponseEntity<Void> response = controller.logout(authorizationHeader);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tokenBlacklistService).revoke(anyString(), any());
    }

    @Test
    @DisplayName("logout: missing Authorization header throws AuthenticationException")
    void logout_missingHeader_shouldThrow() {
        //given
        //when
        //then
        assertThrows(AuthenticationException.class, () -> controller.logout(null));
    }

    @Test
    @DisplayName("changePassword: resolves current username and delegates to facade")
    void changePassword_validRequest_shouldReturnOk() {
        //given
        UpdateCredentialsRequest updateRequest = new UpdateCredentialsRequest("newStrongPassword1");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::currentUsername).thenReturn("John.Doe");

            //when
            ResponseEntity<Void> response = controller.changePassword("Bearer some.jwt.token", updateRequest);

            //then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(gymFacade).changePassword("John.Doe", "newStrongPassword1");
        }
    }
}