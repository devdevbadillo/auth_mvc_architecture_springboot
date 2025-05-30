package com.david.auth_mvc.controller.rest;

import com.david.auth_mvc.model.domain.exceptions.accessToken.AlreadyHaveAccessTokenToChangePasswordException;
import com.david.auth_mvc.model.domain.exceptions.auth.HasAccessWithOAuth2Exception;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotVerifiedException;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.model.infrestructure.utils.constants.CommonConstants;
import com.david.auth_mvc.model.infrestructure.utils.constants.routes.CredentialRoutes;
import com.david.auth_mvc.controller.dto.request.ChangePasswordRequest;
import com.david.auth_mvc.controller.dto.request.RecoveryAccountRequest;
import com.david.auth_mvc.controller.dto.response.PairTokenResponse;
import com.david.auth_mvc.model.domain.entity.Credential;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.david.auth_mvc.model.domain.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.controller.dto.request.SignUpRequest;
import com.david.auth_mvc.controller.dto.response.MessageResponse;
import com.david.auth_mvc.model.business.services.interfaces.application.ICredentialService;

import jakarta.validation.Valid;

@AllArgsConstructor
@RestController
@Validated
@RequestMapping(path = CommonConstants.PUBLIC_URL, produces = { MediaType.APPLICATION_JSON_VALUE } )
public class CredentialController {
    
    private final ICredentialService credentialService;

    @PostMapping(CredentialRoutes.SIGN_UP_URL)
    public ResponseEntity<MessageResponse> signUp(
            @RequestBody @Valid SignUpRequest signUpRequest
    ) throws UserAlreadyExistException{
        return ResponseEntity.ok(credentialService.signUp(signUpRequest));
    }

    @PatchMapping(CredentialRoutes.VERIFY_ACCOUNT_URL)
    public ResponseEntity<PairTokenResponse> verifyAccount(HttpServletRequest request){
        String accessTokenId = (String) request.getAttribute("accessTokenId");

        return ResponseEntity.ok(credentialService.verifyAccount(accessTokenId));
    }

    @PatchMapping(CredentialRoutes.REFRESH_ACCESS_TO_VERIFY_ACCOUNT_URL)
    public ResponseEntity<MessageResponse> refreshAccessToVerifyAccount(HttpServletRequest request){
        Credential credential = (Credential) request.getAttribute("credential");
        String refreshToken = (String) request.getAttribute("refreshToken");

        return ResponseEntity.ok(credentialService.refreshAccessToVerifyAccount(credential, refreshToken));
    }

    @PostMapping(CredentialRoutes.RECOVERY_ACCOUNT_URL)
    public ResponseEntity<MessageResponse> recoveryAccount(
            @RequestBody @Valid RecoveryAccountRequest recoveryAccountRequest
    ) throws UserNotFoundException, HasAccessWithOAuth2Exception, AlreadyHaveAccessTokenToChangePasswordException, UserNotVerifiedException {
        return ResponseEntity.ok(credentialService.recoveryAccount(recoveryAccountRequest));
    }

    @GetMapping(CredentialRoutes.CHANGE_PASSWORD_URL)
    public ResponseEntity<MessageResponse> viewChangePassword() {
        return ResponseEntity.ok(new MessageResponse("Ok"));
    }

    @PatchMapping(CredentialRoutes.CHANGE_PASSWORD_URL)
    public ResponseEntity<MessageResponse> changePassword(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest,
            HttpServletRequest request
    ) {
        Credential credential =(Credential) request.getAttribute("credential");
        String accessTokenId = (String) request.getAttribute("accessTokenId");
        String password = changePasswordRequest.getPassword();

        return ResponseEntity.ok(credentialService.changePassword(credential, password, accessTokenId));
    }
}


