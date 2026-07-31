package io.github.mrergos.gymcrm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT access token issued after successful login")
public record TokenResponse(
        @Schema(description = "Bearer token to use in the Authorization header for subsequent requests")
        String accessToken,

        @Schema(description = "Token type, always 'Bearer'", example = "Bearer")
        String tokenType
) {
    public static TokenResponse bearer(String accessToken) {
        return new TokenResponse(accessToken, "Bearer");
    }
}
