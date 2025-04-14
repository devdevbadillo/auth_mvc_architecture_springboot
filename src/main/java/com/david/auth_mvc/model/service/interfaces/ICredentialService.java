package com.david.auth_mvc.model.service.interfaces;

import com.david.auth_mvc.common.exceptions.accessToken.AlreadyHaveAccessTokenToChangePasswordException;
import com.david.auth_mvc.common.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;

import com.david.auth_mvc.model.domain.dto.request.ChangePasswordRequest;
import com.david.auth_mvc.model.domain.dto.request.RecoveryAccountRequest;
import com.david.auth_mvc.model.domain.dto.request.SignUpRequest;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.entity.Credential;
import jakarta.mail.MessagingException;

public interface ICredentialService {
    MessageResponse signUp(SignUpRequest signUpRequest) throws UserAlreadyExistException;

    void signUp(Credential credential) throws UserAlreadyExistException;

    MessageResponse recoveryAccount(
            RecoveryAccountRequest recoveryAccountRequest
    ) throws UserNotFoundException, HaveAccessWithOAuth2Exception, MessagingException, AlreadyHaveAccessTokenToChangePasswordException;

    MessageResponse changePassword(ChangePasswordRequest changePasswordRequest, String email, String accessTokenId) throws HaveAccessWithOAuth2Exception, UserNotFoundException;

    Credential isRegisteredUser(String email) throws UserNotFoundException;

    void hasAccessWithOAuth2(Credential credential) throws HaveAccessWithOAuth2Exception;
}
