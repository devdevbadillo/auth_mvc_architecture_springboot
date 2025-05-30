package com.david.auth_mvc.controller.doc;

import com.david.auth_mvc.model.domain.exceptions.auth.HasAccessWithOAuth2Exception;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotVerifiedException;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.controller.dto.request.SignInRequest;
import com.david.auth_mvc.controller.dto.response.MessageResponse;
import com.david.auth_mvc.controller.dto.response.PairTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Authentication",
        description = "Authentication endpoint"
)
public interface AuthDoc {

    @Operation(
            summary = "Sign in",
            description = "Authenticate a user and return the access token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"token\": \"*****\", \"refreshToken\": \"*****\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"message\": \"Email or password is incorrect\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"message\": \"Email is required\"}"
                            )
                    )
            )
    })
    ResponseEntity<PairTokenResponse> signIn(@RequestBody @Valid SignInRequest signInRequest) throws BadCredentialsException, HasAccessWithOAuth2Exception, UserNotVerifiedException, UserNotFoundException;

    @Operation(
            summary = "Refresh access token",
            description = "Endpoint to refresh the access token, send the refresh token in the header 'refreshToken'"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Valid token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"token\": \"*****\", \"refreshToken\": \"*****\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"message\": \"Invalid token\"}"
                            )
                    )
            )
    })
    ResponseEntity<PairTokenResponse> refreshToken(@RequestHeader @NotBlank @NotNull String refreshToken);

    @Operation(
            summary = "OAuth2 error",
            description = "Endpoint for OAuth2 error"
    )
    ResponseEntity<MessageResponse> authenticationOAuth2Error();
}
