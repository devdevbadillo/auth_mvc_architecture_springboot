package com.david.auth_mvc.model.service.implementation;

import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;
import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.common.utils.JwtUtil;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.messages.AuthMessages;
import com.david.auth_mvc.common.utils.constants.messages.CredentialMessages;
import com.david.auth_mvc.model.domain.dto.request.ChangePasswordRequest;
import com.david.auth_mvc.model.domain.dto.request.RecoveryAccountRequest;
import com.david.auth_mvc.model.service.interfaces.IEmailService;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.david.auth_mvc.common.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.model.domain.dto.request.SignUpRequest;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.repository.CredentialRepository;
import com.david.auth_mvc.model.service.interfaces.ICredentialService;

import java.util.Date;


@Service
public class CredentialServiceImpl implements ICredentialService{

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IEmailService emailService;

    public CredentialServiceImpl(
            CredentialRepository credentialRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            IEmailService emailService
    ) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @Override
    public MessageResponse signUp(SignUpRequest signUpRequest) throws UserAlreadyExistException{
        this.validateUniqueUser(signUpRequest.getEmail());

        Credential credential = this.buildCredential(signUpRequest);
        credentialRepository.save(credential);
        
        return new MessageResponse(CredentialMessages.USER_CREATED_SUCCESSFULLY);
    }

    @Override
    public MessageResponse recoveryAccount(RecoveryAccountRequest recoveryAccountRequest) throws UserNotFoundException, HaveAccessWithOAuth2Exception, MessagingException {
        this.validateAccess(recoveryAccountRequest.getEmail());

        Date expirationAccessToken = jwtUtil.calculateExpirationMinutesToken(CommonConstants.EXPIRATION_CHANGE_PASSWORD_TOKEN_MINUTES);
        String accessToken = jwtUtil.generateToken(recoveryAccountRequest.getEmail(), expirationAccessToken, CommonConstants.TYPE_CHANGE_PASSWORD );

        emailService.sendEmailRecoveryAccount(recoveryAccountRequest.getEmail(), accessToken);
        return new MessageResponse(CredentialMessages.RECOVERY_ACCOUNT_INSTRUCTIONS_SENT);
    }

    @Override
    public MessageResponse changePassword(ChangePasswordRequest changePasswordRequest, String email) throws HaveAccessWithOAuth2Exception, UserNotFoundException {
        Credential credential = this.validateAccess(email);
        String password = this.encodePassword(changePasswordRequest.getPassword());

        credential.setPassword(password);
        credentialRepository.save(credential);

        return new MessageResponse(CredentialMessages.CHANGE_PASSWORD_SUCCESSFULLY);
    }

    @Override
    public void signUp(Credential credential) throws UserAlreadyExistException{
        this.validateUniqueUser(credential.getEmail());

        credentialRepository.save(credential);
    }

    private Credential isRegisteredUser(String email) throws UserNotFoundException{
        Credential credential = credentialRepository.getCredentialByEmail(email);
        if (credential == null) throw new UserNotFoundException(CredentialMessages.USER_NOT_REGISTERED);
        return credential;
    }

    private Credential validateAccess(String email) throws HaveAccessWithOAuth2Exception, UserNotFoundException{
        Credential credential = this.isRegisteredUser(email);
        if ( credential.getIsAccesOauth() ){
            throw new HaveAccessWithOAuth2Exception(AuthMessages.ACCESS_WITH_OAUTH2_ERROR);
        }
        return credential;
    }

    private void validateUniqueUser(String email) throws UserAlreadyExistException{
        if(credentialRepository.getCredentialByEmail(email) != null){
            throw new UserAlreadyExistException(CredentialMessages.USER_ALREADY_EXISTS);
        }
    }

    private Credential buildCredential(SignUpRequest signUpRequest){
        String passHash = this.encodePassword(signUpRequest.getPassword());
        return Credential.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(passHash)
                .isAccesOauth(false)
                .build();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}

