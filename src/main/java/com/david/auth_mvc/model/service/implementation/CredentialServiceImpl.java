package com.david.auth_mvc.model.service.implementation;

import com.david.auth_mvc.common.utils.constants.errors.CredentialErrors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.david.auth_mvc.common.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.model.domain.dto.request.SignUpRequest;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.repository.CredentialRepository;
import com.david.auth_mvc.model.service.interfaces.ICredentialService;


@Service
public class CredentialServiceImpl implements ICredentialService{

    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    public CredentialServiceImpl(CredentialRepository credentialRepository, PasswordEncoder passwordEncoder) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public MessageResponse signUp(SignUpRequest signUpRequest) throws UserAlreadyExistException{
        this.validateUniqueUser(signUpRequest.getEmail());

        Credential credential = this.buildCredential(signUpRequest);
        credentialRepository.save(credential);
        
        return new MessageResponse(CredentialErrors.USER_CREATED_SUCCESSFULLY);
    }

    @Override
    public MessageResponse signUp(Credential credential) throws UserAlreadyExistException{
        this.validateUniqueUser(credential.getEmail());

        credentialRepository.save(credential);

        return new MessageResponse(CredentialErrors.USER_CREATED_SUCCESSFULLY);
    }


    private Credential buildCredential(SignUpRequest signUpRequest){
        String passHash = passwordEncoder.encode(signUpRequest.getPassword());
        return Credential.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(passHash)
                .isAccesOauth(false)
                .build();
    }

    private void validateUniqueUser(String email) throws UserAlreadyExistException{
        if(credentialRepository.getCredentialByEmail(email) != null){
            throw new UserAlreadyExistException(CredentialErrors.USER_ALREADY_EXISTS);
        }
    }
}
