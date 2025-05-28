package com.david.auth_mvc.model.domain.services.application;

import com.david.auth_mvc.common.exceptions.accessToken.AlreadyHaveAccessTokenToChangePasswordException;
import com.david.auth_mvc.common.exceptions.auth.UserNotVerifiedException;
import com.david.auth_mvc.common.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;

import com.david.auth_mvc.model.domain.dto.request.RecoveryAccountRequest;
import com.david.auth_mvc.model.domain.dto.request.SignUpRequest;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.dto.response.PairTokenResponse;
import com.david.auth_mvc.model.domain.entity.Credential;

public interface ICredentialService {
    MessageResponse signUp(SignUpRequest signUpRequest) throws UserAlreadyExistException;

    void signUp(Credential credential) throws UserAlreadyExistException;

    PairTokenResponse verifyAccount(String accessTokenId);

    MessageResponse refreshAccessToVerifyAccount(Credential credential, String refreshToken);

    MessageResponse recoveryAccount(
            RecoveryAccountRequest recoveryAccountRequest
    ) throws UserNotFoundException, HaveAccessWithOAuth2Exception, AlreadyHaveAccessTokenToChangePasswordException, UserNotVerifiedException;;

    MessageResponse changePassword(Credential credential, String password, String accessTokenId);

    Credential isRegisteredUser(String email) throws UserNotFoundException;

    void hasAccessWithOAuth2(Credential credential) throws HaveAccessWithOAuth2Exception;
}
