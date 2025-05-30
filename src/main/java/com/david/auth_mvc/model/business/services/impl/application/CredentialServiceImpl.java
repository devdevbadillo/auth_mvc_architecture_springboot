package com.david.auth_mvc.model.business.services.impl.application;

import com.david.auth_mvc.model.domain.exceptions.auth.HasAccessWithOAuth2Exception;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotVerifiedException;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.model.infrestructure.mapper.CredentialEntityMapper;
import com.david.auth_mvc.model.infrestructure.utils.constants.CommonConstants;
import com.david.auth_mvc.controller.messages.AuthMessages;
import com.david.auth_mvc.controller.messages.CredentialMessages;
import com.david.auth_mvc.controller.dto.request.RecoveryAccountRequest;
import com.david.auth_mvc.controller.dto.response.PairTokenResponse;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.events.DomainEventPublisher;
import com.david.auth_mvc.model.domain.events.SendEmailEvent;
import com.david.auth_mvc.model.domain.events.UserRegisteredEvent;
import com.david.auth_mvc.model.business.services.interfaces.ITokenService;
import com.david.auth_mvc.model.business.services.interfaces.application.ICredentialService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.david.auth_mvc.model.domain.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.controller.dto.request.SignUpRequest;
import com.david.auth_mvc.controller.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.infrestructure.repository.CredentialRepository;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class CredentialServiceImpl implements ICredentialService {
    private final CredentialEntityMapper    entityMapper;
    private final CredentialRepository      credentialRepository;
    private final DomainEventPublisher      domainEventPublisher;
    private final ITokenService tokenService;

    @Override
    @Transactional(readOnly = false)
    public MessageResponse signUp(SignUpRequest request) throws UserAlreadyExistException {
        this.isUniqueUser(request.getEmail());

        Credential credential = entityMapper.toCredentialEntity(request);
        credentialRepository.save(credential);

        domainEventPublisher.publish(new UserRegisteredEvent(credential));

        return new MessageResponse(CredentialMessages.USER_CREATED_SUCCESSFULLY);
    }

    @Override
    @Transactional(readOnly = false)
    public void signUp(Credential credential) throws UserAlreadyExistException {
        this.isUniqueUser(credential.getEmail());
        credentialRepository.save(credential);
    }

    @Override
    @Transactional(readOnly = false)
    public PairTokenResponse verifyAccount(String accessTokenId) {
        AccessToken accessToken = this.tokenService.getAccessToken(accessTokenId);
        Credential credential = accessToken.getCredential();

        credential.setIsVerified(true);
        this.credentialRepository.save(credential);

        this.tokenService.revokePairTokens(accessTokenId);

        return this.tokenService.generateAuthTokens(credential);
    }

    @Override
    @Transactional(readOnly = false)
    public MessageResponse refreshAccessToVerifyAccount(Credential credential, String refreshToken){
        String accessToken = this.tokenService.saveAccessToken(credential, CommonConstants.TYPE_ACCESS_TOKEN_TO_VERIFY_ACCOUNT, CommonConstants.EXPIRATION_TOKEN_TO_VERIFY_ACCOUNT);

        domainEventPublisher.publish(new SendEmailEvent(credential, accessToken, CommonConstants.TYPE_ACCESS_TOKEN_TO_VERIFY_ACCOUNT, refreshToken));

        return new MessageResponse(CredentialMessages.SEND_EMAIL_VERIFY_ACCOUNT_SUCCESSFULLY);
    }

    @Override
    @Transactional(readOnly = false)
    public MessageResponse recoveryAccount(
            RecoveryAccountRequest recoveryAccountRequest
    ) throws UserNotFoundException, HasAccessWithOAuth2Exception, UserNotVerifiedException{
        Credential credential = this.isRegisteredUser(recoveryAccountRequest.getEmail());

        if(!credential.getIsVerified()) throw new UserNotVerifiedException(AuthMessages.USER_NOT_VERIFIED_ERROR);

        this.hasAccessWithOAuth2(credential);

        String accessToken = this.tokenService.saveAccessToken(credential, CommonConstants.TYPE_ACCESS_TOKEN_TO_CHANGE_PASSWORD,  CommonConstants.EXPIRATION_TOKEN_TO_CHANGE_PASSWORD);

        domainEventPublisher.publish(new SendEmailEvent(credential, accessToken, CommonConstants.TYPE_ACCESS_TOKEN_TO_CHANGE_PASSWORD));

        return new MessageResponse(CredentialMessages.RECOVERY_ACCOUNT_INSTRUCTIONS_SENT);
    }

    @Override
    @Transactional(readOnly = false)
    public MessageResponse changePassword(Credential credential, String password, String accessTokenId) {
        this.tokenService.revokeAccessToken(accessTokenId);

        credential.setPassword(entityMapper.encodePassword(password));
        credentialRepository.save(credential);

        return new MessageResponse(CredentialMessages.CHANGE_PASSWORD_SUCCESSFULLY);
    }

    @Override
    public Credential isRegisteredUser(String email) throws UserNotFoundException{
        Credential credential = credentialRepository.getCredentialByEmail(email);
        if (credential == null) throw new UserNotFoundException(CredentialMessages.USER_NOT_REGISTERED);
        return credential;
    }

    @Override
    public Credential getCredentialByEmail(String email) {
        return credentialRepository.getCredentialByEmail(email);
    }

    @Override
    public void hasAccessWithOAuth2(Credential credential) throws HasAccessWithOAuth2Exception {
        if ( credential.getIsAccesOauth() ){
            throw new HasAccessWithOAuth2Exception(AuthMessages.ACCESS_WITH_OAUTH2_ERROR);
        }
    }

    private void isUniqueUser(String email) throws UserAlreadyExistException{
        if (credentialRepository.getCredentialByEmail(email) != null) throw new UserAlreadyExistException(CredentialMessages.USER_ALREADY_EXISTS);
    }

}

