package com.david.auth_mvc.model.service.interfaces;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;
import com.david.auth_mvc.common.exceptions.auth.UserNotVerifiedException;
import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.model.domain.dto.response.PairTokenResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.david.auth_mvc.model.domain.dto.request.SignInRequest;

public interface IAuthService {

    PairTokenResponse signIn(SignInRequest signInRequest) throws BadCredentialsException, HaveAccessWithOAuth2Exception, UserNotVerifiedException, UserNotFoundException;

    PairTokenResponse refreshToken(String refreshToken) throws JWTVerificationException;
}
