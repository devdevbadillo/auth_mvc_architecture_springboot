package com.david.auth_mvc.model.service.implementation;

import com.david.auth_mvc.common.exceptions.accessToken.AlreadyHaveAccessTokenToChangePasswordException;
import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;
import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.common.mapper.CredentialEntityMapper;
import com.david.auth_mvc.common.utils.JwtUtil;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.messages.AuthMessages;
import com.david.auth_mvc.common.utils.constants.messages.CredentialMessages;
import com.david.auth_mvc.model.domain.dto.request.ChangePasswordRequest;
import com.david.auth_mvc.model.domain.dto.request.RecoveryAccountRequest;
import com.david.auth_mvc.model.service.interfaces.IAccessTokenService;
import com.david.auth_mvc.model.service.interfaces.IEmailService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.david.auth_mvc.common.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.model.domain.dto.request.SignUpRequest;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.repository.CredentialRepository;
import com.david.auth_mvc.model.service.interfaces.ICredentialService;

import java.util.Date;

@AllArgsConstructor
@Service
public class CredentialServiceImpl implements ICredentialService{

    private final CredentialRepository credentialRepository;
    private final CredentialEntityMapper credentialEntityMapper;
    private final IEmailService emailService;
    private final IAccessTokenService accessTokenService;
    private final JwtUtil jwtUtil;

    @Override
    public MessageResponse signUp(SignUpRequest signUpRequest) throws UserAlreadyExistException {
        this.isUniqueUser(signUpRequest.getEmail());
        Credential credential = credentialEntityMapper.toCredentialEntity(signUpRequest);
        credentialRepository.save(credential);

        return new MessageResponse(CredentialMessages.USER_CREATED_SUCCESSFULLY);
    }

    @Override
    public void signUp(Credential credential) throws UserAlreadyExistException {
        this.isUniqueUser(credential.getEmail());
        credentialRepository.save(credential);
    }

    @Override
    public MessageResponse recoveryAccount(
            RecoveryAccountRequest recoveryAccountRequest
    ) throws UserNotFoundException, HaveAccessWithOAuth2Exception, MessagingException, AlreadyHaveAccessTokenToChangePasswordException {
        Credential credential = this.isRegisteredUser(recoveryAccountRequest.getEmail());
        this.hasAccessWithOAuth2(credential);
        this.accessTokenService.hasAccessTokenToChangePassword(credential);

        Date expirationAccessToken = jwtUtil.calculateExpirationMinutesToken(CommonConstants.EXPIRATION_CHANGE_PASSWORD_TOKEN_MINUTES);
        String accessToken = jwtUtil.generateToken(recoveryAccountRequest.getEmail(), expirationAccessToken, CommonConstants.TYPE_CHANGE_PASSWORD );

        emailService.sendEmailRecoveryAccount(recoveryAccountRequest.getEmail(), accessToken);

        this.accessTokenService.saveAccessTokenToChangePassword(accessToken, credential);

        return new MessageResponse(CredentialMessages.RECOVERY_ACCOUNT_INSTRUCTIONS_SENT);
    }

    @Override
    public MessageResponse changePassword(ChangePasswordRequest changePasswordRequest, String email, String accessTokenId) throws HaveAccessWithOAuth2Exception, UserNotFoundException {
        Credential credential = this.isRegisteredUser(email);
        this.hasAccessWithOAuth2(credential);

        credential.setPassword(changePasswordRequest.getPassword());
        credentialRepository.save(credential);

        accessTokenService.deleteAccessToken(accessTokenId);
        return new MessageResponse(CredentialMessages.CHANGE_PASSWORD_SUCCESSFULLY);
    }


    @Override
    public Credential isRegisteredUser(String email) throws UserNotFoundException{
        Credential credential = credentialRepository.getCredentialByEmail(email);
        if (credential == null) throw new UserNotFoundException(CredentialMessages.USER_NOT_REGISTERED);
        return credential;
    }

    @Override
    public void hasAccessWithOAuth2(Credential credential) throws HaveAccessWithOAuth2Exception{
        if ( credential.getIsAccesOauth() ){
            throw new HaveAccessWithOAuth2Exception(AuthMessages.ACCESS_WITH_OAUTH2_ERROR);
        }
    }

    private void isUniqueUser(String email) throws UserAlreadyExistException{
        if (credentialRepository.getCredentialByEmail(email) != null) throw new UserAlreadyExistException(CredentialMessages.USER_ALREADY_EXISTS);
    }

}

