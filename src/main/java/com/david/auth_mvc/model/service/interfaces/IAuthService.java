package com.david.auth_mvc.model.service.interfaces;

import com.david.auth_mvc.model.domain.dto.response.SignInResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.model.domain.dto.request.SignInRequest;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;

public interface IAuthService {

    SignInResponse signIn(SignInRequest signInRequest) throws BadCredentialsException;

    SignInResponse refreshToken(String refreshToken) throws UserNotFoundException;
}
