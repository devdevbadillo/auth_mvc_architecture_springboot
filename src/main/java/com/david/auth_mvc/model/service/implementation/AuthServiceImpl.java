package com.david.auth_mvc.model.service.implementation;

import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;
import com.david.auth_mvc.common.exceptions.auth.UserNotVerifiedException;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.messages.AuthMessages;
import com.david.auth_mvc.common.utils.constants.messages.CredentialMessages;
import com.david.auth_mvc.model.domain.dto.response.PairTokenResponse;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.service.interfaces.*;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.model.domain.dto.request.SignInRequest;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements IAuthService{
    private final PasswordEncoder passwordEncoder;
    private final ITokenService tokenService;
    private final ICredentialService credentialService;

    @Override
    public PairTokenResponse signIn(SignInRequest signInRequest) throws BadCredentialsException, HaveAccessWithOAuth2Exception, UserNotVerifiedException, UserNotFoundException {
        Credential credential = this.credentialService.isRegisteredUser(signInRequest.getEmail());
        this.credentialService.hasAccessWithOAuth2(credential);

        if(!credential.getIsVerified()) throw new UserNotVerifiedException(AuthMessages.USER_NOT_VERIFIED_ERROR);

        Authentication authentication = this.authenticate(credential, signInRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return this.tokenService.generateAuthTokens(credential);
    }

    @Override
    public PairTokenResponse refreshToken(String refreshToken) {
        String accessToken = this.tokenService.refreshAccessTokenToAccessApp(refreshToken);

        return new PairTokenResponse(accessToken, refreshToken);
    }

    private Authentication authenticate(Credential credential, String password) throws BadCredentialsException {
        List<SimpleGrantedAuthority> autorityList = List.of(
                new SimpleGrantedAuthority("ROLE_" + CommonConstants.ROLE_USER)
        );

        if (!passwordEncoder.matches(password, credential.getPassword())) throw new BadCredentialsException(CredentialMessages.PASSWORD_INCORRECT);

        return new UsernamePasswordAuthenticationToken(credential.getEmail(), credential.getPassword(), autorityList);
    }
}
