package com.david.auth_mvc.controller.rest;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;
import com.david.auth_mvc.common.exceptions.auth.UserNotVerifiedException;
import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.routes.AuthRoutes;
import com.david.auth_mvc.controller.doc.AuthDoc;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.dto.response.PairTokenResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.david.auth_mvc.model.domain.dto.request.SignInRequest;
import com.david.auth_mvc.model.service.interfaces.IAuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

@AllArgsConstructor
@RestController
@Validated
@RequestMapping(path= CommonConstants.PUBLIC_URL, produces = { MediaType.APPLICATION_JSON_VALUE })
public class AuthController implements AuthDoc {
    private final IAuthService authService;

    @PostMapping(AuthRoutes.SIGNIN_URL)
    public ResponseEntity<PairTokenResponse> signIn(
        @RequestBody @Valid SignInRequest signInRequest
    ) throws BadCredentialsException, HaveAccessWithOAuth2Exception, UserNotVerifiedException, UserNotFoundException {
        return ResponseEntity.ok(authService.signIn(signInRequest));
    }

    @PostMapping(AuthRoutes.REFRESH_TOKEN_URL)
    public ResponseEntity<PairTokenResponse> refreshToken(
            @RequestHeader @NotBlank @NotNull String refreshToken
    ) throws JWTVerificationException {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @GetMapping(AuthRoutes.OAUTH2_ERROR_URL)
    public ResponseEntity<MessageResponse> authenticationOAuth2Error() {
        return ResponseEntity.ok(new MessageResponse("Authentication error"));
    }
}