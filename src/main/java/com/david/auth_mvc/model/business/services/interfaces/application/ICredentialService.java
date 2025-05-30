package com.david.auth_mvc.model.business.services.interfaces.application;

import com.david.auth_mvc.model.domain.exceptions.credential.UserNotVerifiedException;
import com.david.auth_mvc.model.domain.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.model.domain.exceptions.auth.HasAccessWithOAuth2Exception;

import com.david.auth_mvc.controller.dto.request.RecoveryAccountRequest;
import com.david.auth_mvc.controller.dto.request.SignUpRequest;
import com.david.auth_mvc.controller.dto.response.MessageResponse;
import com.david.auth_mvc.controller.dto.response.PairTokenResponse;
import com.david.auth_mvc.model.domain.entity.Credential;

public interface ICredentialService {
    MessageResponse signUp(SignUpRequest signUpRequest) throws UserAlreadyExistException;

    void signUp(Credential credential) throws UserAlreadyExistException;

    PairTokenResponse verifyAccount(String accessTokenId);

    MessageResponse refreshAccessToVerifyAccount(Credential credential, String refreshToken);

    MessageResponse recoveryAccount(
            RecoveryAccountRequest recoveryAccountRequest
    ) throws UserNotFoundException, HasAccessWithOAuth2Exception, UserNotVerifiedException;;

    Credential getCredentialByEmail(String email);

    MessageResponse changePassword(Credential credential, String password, String accessTokenId);

    Credential isRegisteredUser(String email) throws UserNotFoundException;

    void hasAccessWithOAuth2(Credential credential) throws HasAccessWithOAuth2Exception;
}
