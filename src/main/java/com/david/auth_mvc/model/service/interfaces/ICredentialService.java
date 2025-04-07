package com.david.auth_mvc.model.service.interfaces;

import com.david.auth_mvc.common.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.model.domain.dto.request.SignUpRequest;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.entity.Credential;

public interface ICredentialService {
    public MessageResponse signUp(SignUpRequest signUpRequest) throws UserAlreadyExistException;
    MessageResponse signUp(Credential credential) throws UserAlreadyExistException;

}
