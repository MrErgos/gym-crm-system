package io.github.mrergos.gymcrm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.mrergos.gymcrm.dto.request.UpdateCredentialsRequest;
import io.github.mrergos.gymcrm.facade.Credentials;
import io.github.mrergos.gymcrm.facade.GymFacade;
import io.github.mrergos.gymcrm.security.BasicAuthCredentialsResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController tests")
class AuthControllerTest {

    @Mock
    private GymFacade gymFacade;

    @Mock
    private BasicAuthCredentialsResolver credentialsResolver;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController controller;

    @Test
    @DisplayName("login: resolves credentials, authenticates and returns 200")
    void login_validCredentials_shouldReturnOk() {
        //given
        Credentials credentials = new Credentials("John.Doe", "password123");
        when(credentialsResolver.resolve(request)).thenReturn(credentials);

        //when
        ResponseEntity<Void> response = controller.login(request);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gymFacade).login(credentials);
    }

    @Test
    @DisplayName("changePassword: resolves credentials and delegates to facade")
    void changePassword_validRequest_shouldReturnOk() {
        //given
        Credentials credentials = new Credentials("John.Doe", "password123");
        UpdateCredentialsRequest updateRequest = new UpdateCredentialsRequest("newStrongPassword1");
        when(credentialsResolver.resolve(request)).thenReturn(credentials);

        //when
        ResponseEntity<Void> response = controller.changePassword(request, updateRequest);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gymFacade).changePassword(credentials, "newStrongPassword1");
    }
}