package com.david.auth_mvc.model.service.implementation;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.messages.AuthMessages;
import com.david.auth_mvc.common.utils.constants.messages.CredentialMessages;
import com.david.auth_mvc.model.domain.dto.response.SignInResponse;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.repository.CredentialRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.common.utils.JwtUtil;
import com.david.auth_mvc.model.domain.dto.request.SignInRequest;
import com.david.auth_mvc.model.service.interfaces.IAuthService;

import java.util.Date;

@Service
public class AuthServiceImpl implements IAuthService{

    private final UserDetailsServiceImpl userDetailsServiceImpl;    
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CredentialRepository credentialRepository;

    public AuthServiceImpl(
            UserDetailsServiceImpl userDetailsServiceImpl,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            CredentialRepository credentialRepository
    ) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.credentialRepository = credentialRepository;
    }

    @Override
    public SignInResponse signIn(SignInRequest signInRequest) throws BadCredentialsException, HaveAccessWithOAuth2Exception {
        this.validateAccess(signInRequest.getEmail());
        Authentication authentication = this.authenticate(signInRequest.getEmail(), signInRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = authentication.getPrincipal().toString();
        Date expirationAccessToken = jwtUtil.calculateExpirationMinutesToken(CommonConstants.EXPIRATION_ACCESS_TOKEN_MINUTES);
        Date expirationRefreshToken = jwtUtil.calculateExpirationDaysToken(CommonConstants.EXPIRATION_REFRESH_TOKEN_DAYS);

        String accessToken = jwtUtil.generateToken(username, expirationAccessToken, CommonConstants.TYPE_ACCESS_TOKEN );
        String refreshToken = jwtUtil.generateToken(username, expirationRefreshToken, CommonConstants.TYPE_REFRESH_TOKEN );
        return  new SignInResponse(accessToken, refreshToken);
    }

    @Override
    public SignInResponse refreshToken(String refreshToken) throws UserNotFoundException {
        try {
            DecodedJWT decodedJWT = jwtUtil.validateToken(refreshToken);
            jwtUtil.validateTypeToken(decodedJWT, CommonConstants.TYPE_REFRESH_TOKEN);

            String username = decodedJWT.getSubject();
            Date expirationAccessToken = jwtUtil.calculateExpirationMinutesToken(CommonConstants.EXPIRATION_ACCESS_TOKEN_MINUTES);
            String accessToken = jwtUtil.generateToken(username, expirationAccessToken, CommonConstants.TYPE_ACCESS_TOKEN );
            return new SignInResponse(accessToken, refreshToken);
        } catch (UsernameNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        }
    }

    public Authentication authenticate(String username, String password) throws BadCredentialsException {
        try {
            UserDetails userDetails = this.userDetailsServiceImpl.loadUserByUsername(username);
            if (!passwordEncoder.matches(password, userDetails.getPassword())) throw new BadCredentialsException(CredentialMessages.PASSWORD_INCORRECT);

            return new UsernamePasswordAuthenticationToken(username, userDetails.getPassword(), userDetails.getAuthorities());
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    private void validateAccess(String email) throws HaveAccessWithOAuth2Exception {
        Credential credential = credentialRepository.getCredentialByEmail(email);
        if (credential != null && credential.getIsAccesOauth() ){
            throw new HaveAccessWithOAuth2Exception(AuthMessages.ACCESS_WITH_OAUTH2_ERROR);
        }
    }
}
