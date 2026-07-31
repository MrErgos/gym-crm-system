package io.github.mrergos.gymcrm.security;

import io.github.mrergos.gymcrm.exception.AuthenticationException;
import io.github.mrergos.gymcrm.facade.Credentials;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicAuthCredentialsResolver tests")
class BasicAuthCredentialsResolverTest {

    @Mock
    private HttpServletRequest request;

    private final BasicAuthCredentialsResolver resolver = new BasicAuthCredentialsResolver();

    private String encode(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    @Test
    @DisplayName("resolve: valid header returns credentials")
    void resolve_validHeader_shouldReturnCredentials() {
        //given
        when(request.getHeader("Authorization")).thenReturn(encode("John.Doe", "password123"));

        //when
        Credentials credentials = resolver.resolve(request);

        //then
        assertEquals("John.Doe", credentials.username());
        assertEquals("password123", credentials.password());
    }

    @Test
    @DisplayName("resolve: missing header throws AuthenticationException")
    void resolve_missingHeader_shouldThrow() {
        //given
        when(request.getHeader("Authorization")).thenReturn(null);

        //when //then
        assertThrows(AuthenticationException.class, () -> resolver.resolve(request));
    }

    @Test
    @DisplayName("resolve: header without Basic prefix throws AuthenticationException")
    void resolve_wrongPrefix_shouldThrow() {
        //given
        when(request.getHeader("Authorization")).thenReturn("Bearer sometoken");

        //when //then
        assertThrows(AuthenticationException.class, () -> resolver.resolve(request));
    }

    @Test
    @DisplayName("resolve: invalid Base64 throws AuthenticationException")
    void resolve_invalidBase64_shouldThrow() {
        //given
        when(request.getHeader("Authorization")).thenReturn("Basic not-valid-base64!!!");

        //when //then
        assertThrows(AuthenticationException.class, () -> resolver.resolve(request));
    }

    @Test
    @DisplayName("resolve: missing colon separator throws AuthenticationException")
    void resolve_missingSeparator_shouldThrow() {
        //given
        String encoded = "Basic " + Base64.getEncoder().encodeToString("johndoepassword".getBytes());
        when(request.getHeader("Authorization")).thenReturn(encoded);

        //when //then
        assertThrows(AuthenticationException.class, () -> resolver.resolve(request));
    }

    @Test
    @DisplayName("resolve: blank username throws AuthenticationException")
    void resolve_blankUsername_shouldThrow() {
        //given
        when(request.getHeader("Authorization")).thenReturn(encode("", "password123"));

        //when //then
        assertThrows(AuthenticationException.class, () -> resolver.resolve(request));
    }

    @Test
    @DisplayName("resolve: blank password throws AuthenticationException")
    void resolve_blankPassword_shouldThrow() {
        //given
        when(request.getHeader("Authorization")).thenReturn(encode("John.Doe", ""));

        //when //then
        assertThrows(AuthenticationException.class, () -> resolver.resolve(request));
    }
}