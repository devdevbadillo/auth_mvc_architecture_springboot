package com.david.auth_mvc.controller.rest;

import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.routes.AuthRoutes;
import com.david.auth_mvc.model.domain.dto.response.SignInResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.model.domain.dto.request.SignInRequest;
import com.david.auth_mvc.model.service.interfaces.IAuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

@AllArgsConstructor
@RestController
@Validated
@RequestMapping(path= CommonConstants.PUBLIC_URL, produces = {MediaType.APPLICATION_JSON_VALUE})
@Tag(
        name = "Authentication",
        description = "Authentication endpoint"
)
public class AuthController {
    
    private final IAuthService authService;

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
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"message\": \"Internal server error\"}"
                            )
                    )
            )

    })
    @PostMapping(AuthRoutes.SIGNIN_URL)
    public ResponseEntity<SignInResponse> signIn(
        @RequestBody @Valid SignInRequest signInRequest
    ) throws BadCredentialsException, UserNotFoundException {
        return ResponseEntity.ok(authService.signIn(signInRequest));
    }

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
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"message\": \"refresh token is required\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    example = "{\"message\": \"Internal server error\"}"
                            )
                    )
            )

    })
    @PostMapping(AuthRoutes.REFRESH_TOKEN_URL)
    public ResponseEntity<SignInResponse> refreshToken(
            @RequestHeader @NotBlank @NotNull String refreshToken
    )throws  UserNotFoundException{
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

}