package com.david.auth_mvc.model.business.services.interfaces.application;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.david.auth_mvc.model.domain.exceptions.auth.HasAccessWithOAuth2Exception;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotVerifiedException;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.controller.dto.response.PairTokenResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.david.auth_mvc.controller.dto.request.SignInRequest;

public interface IAuthService {

    PairTokenResponse signIn(SignInRequest signInRequest) throws BadCredentialsException, HasAccessWithOAuth2Exception, UserNotVerifiedException, UserNotFoundException;

    PairTokenResponse refreshToken(String refreshToken) throws JWTVerificationException;
}
