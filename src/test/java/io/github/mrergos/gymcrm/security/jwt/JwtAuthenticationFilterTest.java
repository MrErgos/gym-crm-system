package io.github.mrergos.gymcrm.security.jwt;

import io.github.mrergos.gymcrm.security.GymUserDetailsService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private GymUserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private RequestMatcher publicEndpointsMatcher;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("shouldNotFilter: delegates to publicEndpointsMatcher")
    void shouldNotFilter_delegatesToMatcher() throws ServletException {
        //given
        when(publicEndpointsMatcher.matches(request)).thenReturn(true);

        //when
        boolean result = filter.shouldNotFilter(request);

        //then
        assertTrue(result);
    }

    @Test
    @DisplayName("doFilterInternal: no Authorization header - continues chain without authenticating")
    void doFilterInternal_noAuthorizationHeader_shouldContinueChainOnly() throws ServletException, IOException {
        //given
        when(request.getHeader("Authorization")).thenReturn(null);

        //when
        filter.doFilterInternal(request, response, filterChain);

        //then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal: non-Bearer Authorization header - continues chain without authenticating")
    void doFilterInternal_nonBearerHeader_shouldContinueChainOnly() throws ServletException, IOException {
        //given
        when(request.getHeader("Authorization")).thenReturn("Basic somecredentials");

        //when
        filter.doFilterInternal(request, response, filterChain);

        //then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal: revoked token - continues chain without authenticating")
    void doFilterInternal_revokedToken_shouldContinueChainOnly() throws ServletException, IOException {
        //given
        when(request.getHeader("Authorization")).thenReturn("Bearer some.jwt.token");
        when(jwtService.extractTokenId("some.jwt.token")).thenReturn("jti-123");
        when(tokenBlacklistService.isRevoked("jti-123")).thenReturn(true);

        //when
        filter.doFilterInternal(request, response, filterChain);

        //then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal: valid token - sets authentication in SecurityContext")
    void doFilterInternal_validToken_shouldSetAuthentication() throws ServletException, IOException {
        //given
        UserDetails userDetails = mock(UserDetails.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer some.jwt.token");
        when(jwtService.extractTokenId("some.jwt.token")).thenReturn("jti-123");
        when(tokenBlacklistService.isRevoked("jti-123")).thenReturn(false);
        when(jwtService.extractUsername("some.jwt.token")).thenReturn("John.Doe");
        when(transactionTemplate.execute(any())).thenReturn(userDetails);
        when(jwtService.isTokenValid("some.jwt.token", "John.Doe")).thenReturn(true);

        //when
        filter.doFilterInternal(request, response, filterChain);

        //then
        assertTrue(SecurityContextHolder.getContext().getAuthentication() != null);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal: token fails validation - does not set authentication")
    void doFilterInternal_invalidToken_shouldNotSetAuthentication() throws ServletException, IOException {
        //given
        UserDetails userDetails = mock(UserDetails.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer some.jwt.token");
        when(jwtService.extractTokenId("some.jwt.token")).thenReturn("jti-123");
        when(tokenBlacklistService.isRevoked("jti-123")).thenReturn(false);
        when(jwtService.extractUsername("some.jwt.token")).thenReturn("John.Doe");
        when(transactionTemplate.execute(any())).thenReturn(userDetails);
        when(jwtService.isTokenValid("some.jwt.token", "John.Doe")).thenReturn(false);

        //when
        filter.doFilterInternal(request, response, filterChain);

        //then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal: malformed token throws JwtException - continues chain without authenticating")
    void doFilterInternal_malformedToken_shouldContinueChainOnly() throws ServletException, IOException {
        //given
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed.token");
        when(jwtService.extractTokenId("malformed.token")).thenThrow(new MalformedJwtException("Invalid token"));

        //when
        filter.doFilterInternal(request, response, filterChain);

        //then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal: existing authentication in context is not overwritten")
    void doFilterInternal_existingAuthentication_shouldNotOverwrite() throws ServletException, IOException {
        //given
        Authentication existingAuth = mock(Authentication.class);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(existingAuth);
        SecurityContextHolder.setContext(context);

        when(request.getHeader("Authorization")).thenReturn("Bearer some.jwt.token");
        when(jwtService.extractTokenId("some.jwt.token")).thenReturn("jti-123");
        when(tokenBlacklistService.isRevoked("jti-123")).thenReturn(false);
        when(jwtService.extractUsername("some.jwt.token")).thenReturn("John.Doe");

        //when
        filter.doFilterInternal(request, response, filterChain);

        //then
        assertTrue(SecurityContextHolder.getContext().getAuthentication() == existingAuth);
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(filterChain).doFilter(request, response);
    }
}