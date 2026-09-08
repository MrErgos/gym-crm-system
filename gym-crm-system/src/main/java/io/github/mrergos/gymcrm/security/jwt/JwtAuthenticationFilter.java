package io.github.mrergos.gymcrm.security.jwt;

import io.github.mrergos.gymcrm.security.GymUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final GymUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RequestMatcher publicEndpointsMatcher;
    private final TransactionTemplate transactionTemplate;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   GymUserDetailsService userDetailsService,
                                   TokenBlacklistService tokenBlacklistService,
                                   RequestMatcher publicEndpointsMatcher, TransactionTemplate transactionTemplate) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.publicEndpointsMatcher = publicEndpointsMatcher;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return publicEndpointsMatcher.matches(request);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        try {
            String tokenId = jwtService.extractTokenId(token);
            if (tokenBlacklistService.isRevoked(tokenId)) {
                log.warn("Rejected request with revoked token, jti={}", tokenId);
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = transactionTemplate.execute( status ->
                        userDetailsService.loadUserByUsername(username));

                if (jwtService.isTokenValid(token, username)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authenticated request via JWT for username={}", username);
                }
            }
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT on request {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
