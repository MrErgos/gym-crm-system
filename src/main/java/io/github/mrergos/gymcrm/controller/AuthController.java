package io.github.mrergos.gymcrm.controller;

import io.github.mrergos.gymcrm.dto.request.UpdateCredentialsRequest;
import io.github.mrergos.gymcrm.facade.Credentials;
import io.github.mrergos.gymcrm.facade.GymFacade;
import io.github.mrergos.gymcrm.security.BasicAuthCredentialsResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Credential verification and password management")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final GymFacade gymFacade;
    private final BasicAuthCredentialsResolver credentialsResolver;

    @Autowired
    public AuthController(GymFacade gymFacade, BasicAuthCredentialsResolver credentialsResolver) {
        this.gymFacade = gymFacade;
        this.credentialsResolver = credentialsResolver;
    }

    @GetMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "basicAuth")
    @Operation(
            summary = "Login check",
            description = "Checks that the given username/password (supplied via HTTP Basic Auth) are valid." +
                    "Does not create a session or return a token — every other endpoint authenticates " +
                    "independently using the same header."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credentials are valid"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content)
    })
    public ResponseEntity<Void> login(HttpServletRequest request) {
        Credentials credentials = credentialsResolver.resolve(request);
        log.info("Verifying credentials for username={}", credentials.username());

        gymFacade.login(credentials);

        log.info("Credentials verified successfully for username={}", credentials.username());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "basicAuth")
    @Operation(
            summary = "Change password",
            description = "Changes the caller's own password. The caller authenticates with their " +
                    "current username/password via HTTP Basic Auth, and supplies the new password in the body."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "New password fails validation", content = @Content),
            @ApiResponse(responseCode = "401", description = "Current credentials are invalid", content = @Content)
    })
    public ResponseEntity<Void> changePassword(HttpServletRequest request,
                                               @Valid @RequestBody UpdateCredentialsRequest updateCredentialsRequest) {
        Credentials credentials = credentialsResolver.resolve(request);
        log.info("Change password requested for username={}", credentials.username());

        gymFacade.changePassword( credentials, updateCredentialsRequest.newPassword());

        log.info("Password changed successfully for username={}", credentials.username());
        return ResponseEntity.ok().build();
    }
}
