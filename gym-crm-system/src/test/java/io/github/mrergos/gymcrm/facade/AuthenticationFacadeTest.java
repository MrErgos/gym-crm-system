package io.github.mrergos.gymcrm.facade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationFacade tests")
class AuthenticationFacadeTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationFacade facade;

    @Test
    @DisplayName("authenticate: delegates to AuthenticationManager and returns result")
    void authenticate_valid_shouldDelegateAndReturnAuthentication() {
        //given
        String username = "John.Doe";
        String password = "password123";
        Authentication expected = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(expected);

        //when
        Authentication result = facade.authenticate(username, password);

        //then
        assertEquals(expected, result);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals(username, captor.getValue().getPrincipal());
        assertEquals(password, captor.getValue().getCredentials());
    }

    @Test
    @DisplayName("authenticate: propagates BadCredentialsException")
    void authenticate_wrongPassword_shouldThrowBadCredentialsException() {
        //given
        String username = "John.Doe";
        String password = "wrongPassword";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        //when
        //then
        assertThrows(BadCredentialsException.class,
                () -> facade.authenticate(username, password));
    }

    @Test
    @DisplayName("authenticate: propagates DisabledException when account inactive")
    void authenticate_disabledAccount_shouldThrowDisabledException() {
        //given
        String username = "Inactive.User";
        String password = "password123";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Account is disabled"));

        //when
        //then
        assertThrows(DisabledException.class,
                () -> facade.authenticate(username, password));
    }
}