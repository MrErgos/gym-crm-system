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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Login, logout and password management")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final GymFacade gymFacade;
    private final AuthenticationFacade authenticationFacade;
    private final BasicAuthCredentialsResolver credentialsResolver;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;
    private final GymMetrics gymMetrics;

    public AuthController(GymFacade gymFacade, AuthenticationFacade authenticationFacade,
                          BasicAuthCredentialsResolver credentialsResolver,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          TokenBlacklistService tokenBlacklistService,
                          LoginAttemptService loginAttemptService,
                          GymMetrics gymMetrics) {
        this.gymFacade = gymFacade;
        this.authenticationFacade = authenticationFacade;
        this.credentialsResolver = credentialsResolver;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.loginAttemptService = loginAttemptService;
        this.gymMetrics = gymMetrics;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "basicAuth")
    @Operation(
            summary = "Login",
            description = "Authenticates with username/password (supplied via HTTP Basic Auth) and, on success, " +
                    "issues a JWT access token to be used as a Bearer token on subsequent requests. " +
                    "Accounts are temporarily locked out after repeated failed attempts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, access token issued"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content),
            @ApiResponse(responseCode = "423", description = "Account temporarily locked due to repeated failed logins", content = @Content)
    })
    public TokenResponse login(HttpServletRequest request) {
        Credentials credentials = credentialsResolver.resolve(request);
        String username = credentials.username();

        if (loginAttemptService.isBlocked(username)) {
            log.warn("Login rejected: account temporarily locked, username={}", username);
            throw new AccountLockedException("Account is temporarily locked due to repeated failed login attempts");
        }

        log.info("Login attempt for username={}", username);

        try {
            gymMetrics.recordAuthenticationTime(() -> authenticationFacade.authenticate(
                    username, credentials.password()));
        } catch (BadCredentialsException e) {
            loginAttemptService.onFailedLogin(username);
            gymMetrics.recordAuthenticationFailure();
            throw new AuthenticationException("Invalid username or password");
        }

        loginAttemptService.onSuccessfulLogin(username);
        gymMetrics.recordAuthenticationSuccess();

        String token = jwtService.generateToken(username);
        log.info("Login successful, access token issued for username={}", username);

        return TokenResponse.bearer(token);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Logout",
            description = "Revokes the JWT access token supplied in the Authorization header so it can no " +
                    "longer be used to authenticate requests."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content)
    })
    public ResponseEntity<Void> logout(@Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationException("Missing or invalid Authorization header");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        String tokenId = jwtService.extractTokenId(token);
        Date expiration = jwtService.extractExpiration(token);

        tokenBlacklistService.revoke(tokenId, expiration.toInstant());
        log.info("User logged out, jti={}", tokenId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Change password",
            description = "Changes the caller's own password."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "New password fails validation", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    public ResponseEntity<Void> changePassword(@Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
                                               @Valid @RequestBody UpdateCredentialsRequest updateCredentialsRequest) {
        String username = SecurityUtils.currentUsername();
        log.info("Change password requested for username={}", username);

        gymFacade.changePassword(username, updateCredentialsRequest.newPassword());

        log.info("Password changed successfully for username={}", username);
        return ResponseEntity.ok().build();
    }
}
