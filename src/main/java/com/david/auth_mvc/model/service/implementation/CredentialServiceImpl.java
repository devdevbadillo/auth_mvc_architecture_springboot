package com.david.auth_mvc.model.service.implementation;

import com.david.auth_mvc.common.exceptions.accessToken.AlreadyHaveAccessTokenToChangePasswordException;
import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;
import com.david.auth_mvc.common.exceptions.auth.UserNotVerifiedException;
import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.common.mapper.CredentialEntityMapper;
import com.david.auth_mvc.common.utils.JwtUtil;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.messages.AuthMessages;
import com.david.auth_mvc.common.utils.constants.messages.CredentialMessages;
import com.david.auth_mvc.model.domain.dto.request.ChangePasswordRequest;
import com.david.auth_mvc.model.domain.dto.request.RecoveryAccountRequest;
import com.david.auth_mvc.model.domain.dto.response.SignInResponse;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.service.interfaces.IAccessTokenService;
import com.david.auth_mvc.model.service.interfaces.IEmailService;
import com.david.auth_mvc.model.service.interfaces.IRefreshTokenService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.david.auth_mvc.common.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.model.domain.dto.request.SignUpRequest;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.repository.CredentialRepository;
import com.david.auth_mvc.model.service.interfaces.ICredentialService;

@AllArgsConstructor
@Service
public class CredentialServiceImpl implements ICredentialService{

    private final CredentialRepository credentialRepository;
    private final CredentialEntityMapper credentialEntityMapper;
    private final IEmailService emailService;
    private final IAccessTokenService accessTokenService;
    private final IRefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Override
    public MessageResponse signUp(SignUpRequest signUpRequest) throws UserAlreadyExistException {
        this.isUniqueUser(signUpRequest.getEmail());
        Credential credential = credentialEntityMapper.toCredentialEntity(signUpRequest);
        credentialRepository.save(credential);

        String accessToken = jwtUtil.generateAccessToken(credential, CommonConstants.EXPIRATION_TOKEN_TO_VERIFY_ACCOUNT, CommonConstants.TYPE_VERIFY_ACCOUNT );
        String refreshToken = jwtUtil.generateRefreshToken(credential, CommonConstants.EXPIRATION_REFRESH_TOKEN_TO_VERIFY_ACCOUNT, CommonConstants.TYPE_REFRESH_TOKEN_TO_VERIFY_ACCOUNT );

        AccessToken accessTokenEntity = this.accessTokenService.saveAccessToken(accessToken, credential, CommonConstants.TYPE_VERIFY_ACCOUNT);
        this.refreshTokenService.saveRefreshToken(refreshToken, credential, accessTokenEntity, CommonConstants.TYPE_REFRESH_TOKEN_TO_VERIFY_ACCOUNT);

        this.emailService.sendEmailVerifyAccount(credential.getEmail(), accessToken, refreshToken);

        return new MessageResponse(CredentialMessages.USER_CREATED_SUCCESSFULLY);
    }

    @Override
    public void signUp(Credential credential) throws UserAlreadyExistException {
        this.isUniqueUser(credential.getEmail());
        credentialRepository.save(credential);
    }

    @Override
    public SignInResponse verifyAccount(String accessTokenId) {
        AccessToken accessTokenToVerifyAccount = this.accessTokenService.getTokenByAccessTokenId(accessTokenId);

        Credential credential = accessTokenToVerifyAccount.getCredential();
        credential.setIsVerified(true);
        this.credentialRepository.save(credential);

        this.refreshTokenService.deleteRefreshToken(accessTokenToVerifyAccount);

        String accessToken = jwtUtil.generateAccessToken(credential, CommonConstants.EXPIRATION_TOKEN_TO_ACCESS_APP, CommonConstants.TYPE_ACCESS_TOKEN );
        String refreshToken = jwtUtil.generateRefreshToken(credential, CommonConstants.EXPIRATION_REFRESH_TOKEN_TO_ACCESS_APP, CommonConstants.TYPE_REFRESH_TOKEN );

        AccessToken accessTokenEntity = accessTokenService.saveAccessTokenToAccessApp(accessToken, credential);
        refreshTokenService.saveRefreshToken(refreshToken, credential, accessTokenEntity, CommonConstants.TYPE_REFRESH_TOKEN);
        return new SignInResponse(accessToken, refreshToken);
    }

    @Override
    public MessageResponse refreshAccessToVerifyAccount(
            String refreshToken,
            String email
    ) throws UserNotFoundException, AlreadyHaveAccessTokenToChangePasswordException {
        Credential credential = this.isRegisteredUser(email);
        this.accessTokenService.hasAccessToken(credential, CommonConstants.TYPE_VERIFY_ACCOUNT);

        String accessToken = jwtUtil.generateAccessToken(credential, CommonConstants.EXPIRATION_TOKEN_TO_VERIFY_ACCOUNT, CommonConstants.TYPE_VERIFY_ACCOUNT );

        this.accessTokenService.saveAccessToken(accessToken, credential, CommonConstants.TYPE_VERIFY_ACCOUNT);

        this.emailService.sendEmailVerifyAccount(credential.getEmail(), accessToken, refreshToken);

        return new MessageResponse(CredentialMessages.SEND_EMAIL_VERIFY_ACCOUNT_SUCCESSFULLY);
    }

    @Override
    public MessageResponse recoveryAccount(
            RecoveryAccountRequest recoveryAccountRequest
    ) throws UserNotFoundException, HaveAccessWithOAuth2Exception, AlreadyHaveAccessTokenToChangePasswordException, UserNotVerifiedException{
        Credential credential = this.isRegisteredUser(recoveryAccountRequest.getEmail());
        if(!credential.getIsVerified()) throw new UserNotVerifiedException(AuthMessages.USER_NOT_VERIFIED_ERROR);

        this.hasAccessWithOAuth2(credential);
        this.accessTokenService.hasAccessToken(credential, CommonConstants.TYPE_CHANGE_PASSWORD);

        String accessToken = jwtUtil.generateAccessToken(credential, CommonConstants.EXPIRATION_TOKEN_TO_CHANGE_PASSWORD, CommonConstants.TYPE_CHANGE_PASSWORD );
        this.accessTokenService.saveAccessToken(accessToken, credential, CommonConstants.TYPE_CHANGE_PASSWORD);

        emailService.sendEmailRecoveryAccount(recoveryAccountRequest.getEmail(), accessToken);

        return new MessageResponse(CredentialMessages.RECOVERY_ACCOUNT_INSTRUCTIONS_SENT);
    }

    @Override
    public MessageResponse changePassword(
            ChangePasswordRequest changePasswordRequest,
            String email,
            String accessTokenId
    ) throws HaveAccessWithOAuth2Exception, UserNotFoundException {
        Credential credential = this.isRegisteredUser(email);
        this.hasAccessWithOAuth2(credential);

        String hashPassword = this.credentialEntityMapper.encodePassword(changePasswordRequest.getPassword());
        credential.setPassword(hashPassword);
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

