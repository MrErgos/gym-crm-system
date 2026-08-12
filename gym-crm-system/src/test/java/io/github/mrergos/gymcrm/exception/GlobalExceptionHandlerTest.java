package io.github.mrergos.gymcrm.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler tests")
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @BeforeEach
    void setUp() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/trainees/john.doe");
    }

    @Test
    @DisplayName("handleAuthentication: builds 401 response")
    void handleAuthentication_shouldReturnUnauthorized() {
        //given
        AuthenticationException ex = new AuthenticationException("Invalid username or password");

        //when
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleAuthentication(ex, request);

        //then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody().message());
        assertEquals("/api/v1/trainees/john.doe", response.getBody().path());
    }

    @Test
    @DisplayName("handleNotFound: builds 404 response")
    void handleNotFound_shouldReturnNotFound() {
        //given
        EntityNotFoundException ex = new EntityNotFoundException("Trainee not found: john.doe");

        //when
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleNotFound(ex, request);

        //then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Trainee not found: john.doe", response.getBody().message());
    }

    @Test
    @DisplayName("handleValidation: combines multiple field error messages")
    void handleValidation_multipleErrors_shouldJoinMessages() {
        //given
        FieldError error1 = new FieldError("request", "firstName", "First name must not be blank");
        FieldError error2 = new FieldError("request", "lastName", "Last name must not be blank");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        //when
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleValidation(validationException, request);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("First name must not be blank; Last name must not be blank", response.getBody().message());
    }

    @Test
    @DisplayName("handleValidation: no field errors falls back to default message")
    void handleValidation_noErrors_shouldUseDefaultMessage() {
        //given
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        //when
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleValidation(validationException, request);

        //then
        assertEquals("Validation failed", response.getBody().message());
    }

    @Test
    @DisplayName("handleIllegalArgument: builds 400 response")
    void handleIllegalArgument_shouldReturnBadRequest() {
        //given
        IllegalArgumentException ex = new IllegalArgumentException("Password must be at least 10 characters");

        //when
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleIllegalArgument(ex, request);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password must be at least 10 characters", response.getBody().message());
    }

    @Test
    @DisplayName("handleUnexpected: builds 500 response with generic message")
    void handleUnexpected_shouldReturnInternalServerError() {
        //given
        Exception ex = new RuntimeException("Something broke");

        //when
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleUnexpected(ex, request);

        //then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().message());
    }
}