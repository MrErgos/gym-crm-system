package io.github.mrergos.workinghours.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static final String URI = "/api/v1/trainers/John.Doe/workload";

    @BeforeEach
    void setUp() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(URI);
    }

    @Test
    @DisplayName("handleNotFound returns 404 with exception message and path")
    void handlesEntityNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Trainer not found: John.Doe");

        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Trainer not found: John.Doe");
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().path()).isEqualTo(URI);
    }

    @Test
    @DisplayName("handleAuthentication returns 401 with exception message")
    void handlesAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Invalid service token");

        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleAuthentication(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Invalid service token");
    }

    @Test
    @DisplayName("handleAccessDenied returns 403 with generic message")
    void handlesAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("denied");

        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("Access denied");
    }

    @Test
    @DisplayName("handleValidation returns 400 joining all field error messages")
    void handlesValidationWithMultipleErrors() {
        MethodArgumentNotValidException ex = mockValidationException(
                List.of(
                        new FieldError("request", "trainerUsername", "Trainer username must not be blank"),
                        new FieldError("request", "trainingDuration", "Training duration must be a positive number of minutes")
                )
        );

        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo(
                "Trainer username must not be blank; Training duration must be a positive number of minutes");
    }

    @Test
    @DisplayName("handleValidation falls back to default message when no field errors present")
    void handlesValidationWithNoFieldErrors() {
        MethodArgumentNotValidException ex = mockValidationException(List.of());

        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
    }

    @Test
    @DisplayName("handleIllegalArgument returns 400 with exception message")
    void handlesIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Workload request must not be null");

        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Workload request must not be null");
    }

    @Test
    @DisplayName("handleUnexpected returns 500 with generic message, hiding internal details")
    void handlesUnexpectedException() {
        RuntimeException ex = new RuntimeException("some internal db failure with sensitive info");

        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleUnexpected(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Internal server error");
    }

    @Test
    @DisplayName("ApiError body always includes timestamp, status, error and path")
    void apiErrorContainsAllFields() {
        EntityNotFoundException ex = new EntityNotFoundException("not found");

        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleNotFound(ex, request);
        GlobalExceptionHandler.ApiError body = response.getBody();

        assertThat(body.timestamp()).isNotNull();
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.error()).isEqualTo("Not Found");
        assertThat(body.path()).isEqualTo(URI);
    }

    private MethodArgumentNotValidException mockValidationException(List<FieldError> fieldErrors) {
        MethodArgumentNotValidException ex = org.mockito.Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        return ex;
    }
}
