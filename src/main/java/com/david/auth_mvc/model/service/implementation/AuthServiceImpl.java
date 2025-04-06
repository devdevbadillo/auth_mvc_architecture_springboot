package com.david.auth_mvc.model.service.implementation;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.david.auth_mvc.common.utils.constants.errors.CredentialErrors;
import com.david.auth_mvc.model.domain.dto.response.SignInResponse;
import lombok.AllArgsConstructor;
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

@AllArgsConstructor
@Service
public class AuthServiceImpl implements IAuthService{

    private final UserDetailsServiceImpl userDetailsServiceImpl;    
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final Integer expirationMinutes = 5;
    private final Integer expirationDays = 7;

    @Override
    public SignInResponse signIn(SignInRequest signInRequest) throws BadCredentialsException {
        Authentication authentication = this.authenticate(signInRequest.getEmail(), signInRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = authentication.getPrincipal().toString();
        String accessToken = jwtUtil.generateToken(username, expirationMinutes, 0);
        String refreshToken = jwtUtil.generateToken(username, 0, expirationDays);
        return  new SignInResponse(accessToken, refreshToken);
    }

    @Override
    public SignInResponse refreshToken(String refreshToken) throws UserNotFoundException {
        try {
            DecodedJWT decodedJWT = jwtUtil.validateToken(refreshToken);
            jwtUtil.validateTypeToken(decodedJWT, "refresh_token");

            String username = decodedJWT.getSubject();
            String accessToken = jwtUtil.generateToken(username, expirationMinutes, 0);
            return new SignInResponse(accessToken, refreshToken);
        } catch (UsernameNotFoundException e) {
            throw new UserNotFoundException(e.getMessage());
        }
    }

    public Authentication authenticate(String username, String password) throws BadCredentialsException {
        try {
            UserDetails userDetails = this.userDetailsServiceImpl.loadUserByUsername(username);
            if (!passwordEncoder.matches(password, userDetails.getPassword())) throw new BadCredentialsException(CredentialErrors.PASSWORD_INCORRECT);

            return new UsernamePasswordAuthenticationToken(username, userDetails.getPassword(), userDetails.getAuthorities());
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

}
