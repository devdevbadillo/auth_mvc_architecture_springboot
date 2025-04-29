package com.david.auth_mvc.model.service.implementation;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;
import com.david.auth_mvc.common.exceptions.auth.UserNotVerifiedException;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.messages.AuthMessages;
import com.david.auth_mvc.common.utils.constants.messages.CredentialMessages;
import com.david.auth_mvc.model.domain.dto.response.SignInResponse;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.RefreshToken;
import com.david.auth_mvc.model.service.interfaces.IAccessTokenService;
import com.david.auth_mvc.model.service.interfaces.ICredentialService;
import com.david.auth_mvc.model.service.interfaces.IRefreshTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.common.utils.JwtUtil;
import com.david.auth_mvc.model.domain.dto.request.SignInRequest;
import com.david.auth_mvc.model.service.interfaces.IAuthService;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements IAuthService{
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ICredentialService credentialService;
    private final IAccessTokenService accessTokenService;
    private final IRefreshTokenService refreshTokenService;

    @Override
    public SignInResponse signIn(SignInRequest signInRequest) throws BadCredentialsException, HaveAccessWithOAuth2Exception, UserNotVerifiedException {
        try{
            Credential credential = this.credentialService.isRegisteredUser(signInRequest.getEmail());
            this.credentialService.hasAccessWithOAuth2(credential);

            if(!credential.getIsVerified()) throw new UserNotVerifiedException(AuthMessages.USER_NOT_VERIFIED_ERROR);

            Authentication authentication = this.authenticate(credential, signInRequest.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtUtil.generateAccessToken(credential, CommonConstants.EXPIRATION_TOKEN_TO_ACCESS_APP, CommonConstants.TYPE_ACCESS_TOKEN );
            String refreshToken = jwtUtil.generateRefreshToken(credential, CommonConstants.EXPIRATION_REFRESH_TOKEN_TO_ACCESS_APP, CommonConstants.TYPE_REFRESH_TOKEN );

            AccessToken accessTokenEntity = accessTokenService.saveAccessTokenToAccessApp(accessToken, credential);
            refreshTokenService.saveRefreshToken(refreshToken, credential, accessTokenEntity, CommonConstants.TYPE_REFRESH_TOKEN);
            return new SignInResponse(accessToken, refreshToken);
        }catch (UserNotFoundException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    @Override
    public SignInResponse refreshToken(String refreshToken) {
        DecodedJWT decodedJWT = jwtUtil.validateToken(refreshToken);
        jwtUtil.validateTypeToken(decodedJWT, CommonConstants.TYPE_REFRESH_TOKEN);

        RefreshToken refreshTokenEntity = this.refreshTokenService.findRefreshTokenByRefreshTokenId(decodedJWT.getClaim("jti").asString());

        String accessToken = jwtUtil.generateAccessToken(refreshTokenEntity.getCredential(), CommonConstants.EXPIRATION_TOKEN_TO_ACCESS_APP, CommonConstants.TYPE_ACCESS_TOKEN );

        this.accessTokenService.saveAccessTokenToAccessAppWithRefreshToken(refreshTokenEntity.getAccessToken(), accessToken);
        return new SignInResponse(accessToken, refreshToken);
    }

    private Authentication authenticate(Credential credential, String password) throws BadCredentialsException {
        List<SimpleGrantedAuthority> autorityList = List.of(
                new SimpleGrantedAuthority("ROLE_" + CommonConstants.ROLE_USER)
        );

        if (!passwordEncoder.matches(password, credential.getPassword())) throw new BadCredentialsException(CredentialMessages.PASSWORD_INCORRECT);

        return new UsernamePasswordAuthenticationToken(credential.getEmail(), credential.getPassword(), autorityList);
    }
}
