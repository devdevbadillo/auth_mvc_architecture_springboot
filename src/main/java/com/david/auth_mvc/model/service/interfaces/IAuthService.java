package com.david.auth_mvc.model.service.interfaces;

import org.springframework.security.authentication.BadCredentialsException;

import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.model.domain.dto.request.SignInRequest;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;

public interface IAuthService {
    
    MessageResponse signIn(SignInRequest signInRequest) throws BadCredentialsException;
}
