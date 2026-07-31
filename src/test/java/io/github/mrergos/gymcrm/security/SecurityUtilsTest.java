package io.github.mrergos.gymcrm.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityUtils tests")
class SecurityUtilsTest {

    @Mock
    private Authentication authentication;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("currentUsername: returns authentication name when authentication is present")
    void currentUsername_authenticationPresent_shouldReturnName() {
        //given
        when(authentication.getName()).thenReturn("John.Doe");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        //when
        String result = SecurityUtils.currentUsername();

        //then
        assertEquals("John.Doe", result);
    }

    @Test
    @DisplayName("currentUsername: returns null when authentication is absent")
    void currentUsername_noAuthentication_shouldReturnNull() {
        //given
        SecurityContextHolder.clearContext();

        //when
        String result = SecurityUtils.currentUsername();

        //then
        assertNull(result);
    }
}