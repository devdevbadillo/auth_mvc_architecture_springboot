package com.david.auth_mvc.model.services;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.david.auth_mvc.model.domain.exceptions.auth.HasAccessWithOAuth2Exception;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotVerifiedException;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.model.infrestructure.utils.JwtUtil;
import com.david.auth_mvc.model.infrestructure.utils.constants.CommonConstants;
import com.david.auth_mvc.controller.messages.AuthMessages;
import com.david.auth_mvc.controller.messages.CredentialMessages;
import com.david.auth_mvc.controller.dto.request.SignInRequest;
import com.david.auth_mvc.controller.dto.response.PairTokenResponse;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.RefreshToken;
import com.david.auth_mvc.model.business.services.impl.application.AuthServiceImpl;
import com.david.auth_mvc.model.business.services.interfaces.IAccessTokenService;
import com.david.auth_mvc.model.business.services.interfaces.application.ICredentialService;
import com.david.auth_mvc.model.business.services.interfaces.IRefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AuthServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ICredentialService credentialService;

    @Mock
    private IAccessTokenService accessTokenService;

    @Mock
    private IRefreshTokenService refreshTokenService;

    @Mock
    private DecodedJWT decodedJWT;

    @InjectMocks
    private AuthServiceImpl authService;

    private Credential mockCredential;
    private SignInRequest mockSignInRequest;
    private String mockAccessToken;
    private String mockRefreshToken;
    private AccessToken mockAccessTokenEntity;
    private RefreshToken mockRefreshTokenEntity;

    @BeforeEach
    void setup() {
        mockCredential = new Credential();
        mockCredential.setEmail("test@example.com");
        mockCredential.setPassword("encodedPassword");
        mockCredential.setIsVerified(true);

        mockSignInRequest = new SignInRequest();
        mockSignInRequest.setEmail("test@example.com");
        mockSignInRequest.setPassword("password123");

        mockAccessToken = "mock.access.token";
        mockRefreshToken = "mock.refresh.token";

        mockAccessTokenEntity = new AccessToken();
        mockRefreshTokenEntity = new RefreshToken();
        mockRefreshTokenEntity.setCredential(mockCredential);
        mockRefreshTokenEntity.setAccessToken(mockAccessTokenEntity);
    }

    @Test
    @DisplayName("Sign In - Success Case")
    void signIn_Success() throws UserNotFoundException, HasAccessWithOAuth2Exception, UserNotVerifiedException {
        // Arrange
        when(credentialService.isRegisteredUser(mockSignInRequest.getEmail())).thenReturn(mockCredential);
        doNothing().when(credentialService).hasAccessWithOAuth2(mockCredential);
        when(passwordEncoder.matches(mockSignInRequest.getPassword(), mockCredential.getPassword())).thenReturn(true);
        when(jwtUtil.generateAccessToken(eq(mockCredential), anyInt(), eq(CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP)))
                .thenReturn(mockAccessToken);
        when(jwtUtil.generateRefreshToken(eq(mockCredential), anyInt(), eq(CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP)))
                .thenReturn(mockRefreshToken);
        when(accessTokenService.saveAccessTokenToAccessApp(mockAccessToken, mockCredential)).thenReturn(mockAccessTokenEntity);
        doNothing().when(refreshTokenService).saveRefreshToken(mockRefreshToken, mockCredential, mockAccessTokenEntity, CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP);

        // Act
        PairTokenResponse response = authService.signIn(mockSignInRequest);

        // Assert
        assertNotNull(response);
        assertEquals(mockAccessToken, response.getAccessToken());
        assertEquals(mockRefreshToken, response.getRefreshToken());
        verify(credentialService).isRegisteredUser(mockSignInRequest.getEmail());
        verify(credentialService).hasAccessWithOAuth2(mockCredential);
        verify(passwordEncoder).matches(mockSignInRequest.getPassword(), mockCredential.getPassword());
        verify(jwtUtil).generateAccessToken(eq(mockCredential), anyInt(), eq(CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP));
        verify(jwtUtil).generateRefreshToken(eq(mockCredential), anyInt(), eq(CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP));
        verify(accessTokenService).saveAccessTokenToAccessApp(mockAccessToken, mockCredential);
        verify(refreshTokenService).saveRefreshToken(mockRefreshToken, mockCredential, mockAccessTokenEntity, CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP);
    }

    @Test
    @DisplayName("Sign In - User Not Found")
    void signIn_UserNotFound() throws UserNotFoundException {
        // Arrange
        when(credentialService.isRegisteredUser(mockSignInRequest.getEmail()))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.signIn(mockSignInRequest);
        });
        assertEquals("User not found", exception.getMessage());
        verify(credentialService).isRegisteredUser(mockSignInRequest.getEmail());
        verifyNoMoreInteractions(jwtUtil, accessTokenService, refreshTokenService);
    }

    @Test
    @DisplayName("Sign In - User Has OAuth2 Access")
    void signIn_UserHasOAuth2Access() throws UserNotFoundException, HasAccessWithOAuth2Exception {
        // Arrange
        when(credentialService.isRegisteredUser(mockSignInRequest.getEmail())).thenReturn(mockCredential);
        doThrow(new HasAccessWithOAuth2Exception("User has OAuth2 access")).when(credentialService).hasAccessWithOAuth2(mockCredential);

        // Act & Assert
        HasAccessWithOAuth2Exception exception = assertThrows(HasAccessWithOAuth2Exception.class, () -> {
            authService.signIn(mockSignInRequest);
        });
        assertEquals("User has OAuth2 access", exception.getMessage());
        verify(credentialService).isRegisteredUser(mockSignInRequest.getEmail());
        verify(credentialService).hasAccessWithOAuth2(mockCredential);
        verifyNoMoreInteractions(jwtUtil, accessTokenService, refreshTokenService);
    }

    @Test
    @DisplayName("Sign In - User Not Verified")
    void signIn_UserNotVerified() throws UserNotFoundException, HasAccessWithOAuth2Exception {
        // Arrange
        mockCredential.setIsVerified(false);
        when(credentialService.isRegisteredUser(mockSignInRequest.getEmail())).thenReturn(mockCredential);
        doNothing().when(credentialService).hasAccessWithOAuth2(mockCredential);

        // Act & Assert
        UserNotVerifiedException exception = assertThrows(UserNotVerifiedException.class, () -> {
            authService.signIn(mockSignInRequest);
        });
        assertEquals(AuthMessages.USER_NOT_VERIFIED_ERROR, exception.getMessage());
        verify(credentialService).isRegisteredUser(mockSignInRequest.getEmail());
        verify(credentialService).hasAccessWithOAuth2(mockCredential);
        verifyNoMoreInteractions(jwtUtil, accessTokenService, refreshTokenService);
    }

    @Test
    @DisplayName("Sign In - Invalid Password")
    void signIn_InvalidPassword() throws UserNotFoundException, HasAccessWithOAuth2Exception {
        // Arrange
        when(credentialService.isRegisteredUser(mockSignInRequest.getEmail())).thenReturn(mockCredential);
        doNothing().when(credentialService).hasAccessWithOAuth2(mockCredential);
        when(passwordEncoder.matches(mockSignInRequest.getPassword(), mockCredential.getPassword())).thenReturn(false);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.signIn(mockSignInRequest);
        });
        assertEquals(CredentialMessages.PASSWORD_INCORRECT, exception.getMessage());
        verify(credentialService).isRegisteredUser(mockSignInRequest.getEmail());
        verify(credentialService).hasAccessWithOAuth2(mockCredential);
        verify(passwordEncoder).matches(mockSignInRequest.getPassword(), mockCredential.getPassword());
        verifyNoMoreInteractions(jwtUtil, accessTokenService, refreshTokenService);
    }

    @Test
    @DisplayName("Refresh Token - Success Case")
    void refreshToken_Success() {
        // Arrange
        String refreshTokenId = "refresh-token-id";
        when(jwtUtil.validateToken(mockRefreshToken)).thenReturn(decodedJWT);
        doNothing().when(jwtUtil).validateTypeToken(decodedJWT, CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP);
        when(decodedJWT.getClaim("jti")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("jti").asString()).thenReturn(refreshTokenId);
        when(refreshTokenService.findRefreshTokenByRefreshTokenId(refreshTokenId)).thenReturn(mockRefreshTokenEntity);
        when(jwtUtil.generateAccessToken(eq(mockCredential), anyInt(), eq(CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP)))
                .thenReturn(mockAccessToken);
        doNothing().when(accessTokenService).saveAccessTokenToAccessAppWithRefreshToken(mockAccessTokenEntity, mockAccessToken);

        // Act
        PairTokenResponse response = authService.refreshToken(mockRefreshToken);

        // Assert
        assertNotNull(response);
        assertEquals(mockAccessToken, response.getAccessToken());
        assertEquals(mockRefreshToken, response.getRefreshToken());
        verify(jwtUtil).validateToken(mockRefreshToken);
        verify(jwtUtil).validateTypeToken(decodedJWT, CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP);
        verify(refreshTokenService).findRefreshTokenByRefreshTokenId(refreshTokenId);
        verify(jwtUtil).generateAccessToken(eq(mockCredential), anyInt(), eq(CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP));
        verify(accessTokenService).saveAccessTokenToAccessAppWithRefreshToken(mockAccessTokenEntity, mockAccessToken);
    }

    @Test
    @DisplayName("Refresh Token - Invalid Token")
    void refreshToken_InvalidToken() {
        // Arrange
        when(jwtUtil.validateToken(mockRefreshToken)).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(mockRefreshToken);
        });
        assertEquals("Invalid token", exception.getMessage());
        verify(jwtUtil).validateToken(mockRefreshToken);
        verifyNoMoreInteractions(refreshTokenService, accessTokenService);
    }

    @Test
    @DisplayName("Refresh Token - Invalid Token Type")
    void refreshToken_InvalidTokenType() {
        // Arrange
        when(jwtUtil.validateToken(mockRefreshToken)).thenReturn(decodedJWT);
        doThrow(new RuntimeException("Invalid token type")).when(jwtUtil).validateTypeToken(decodedJWT, CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(mockRefreshToken);
        });
        assertEquals("Invalid token type", exception.getMessage());
        verify(jwtUtil).validateToken(mockRefreshToken);
        verify(jwtUtil).validateTypeToken(decodedJWT, CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP);
        verifyNoMoreInteractions(refreshTokenService, accessTokenService);
    }

    @Test
    @DisplayName("Refresh Token - Refresh Token Not Found")
    void refreshToken_RefreshTokenNotFound() {
        // Arrange
        String refreshTokenId = "refresh-token-id";
        when(jwtUtil.validateToken(mockRefreshToken)).thenReturn(decodedJWT);
        doNothing().when(jwtUtil).validateTypeToken(decodedJWT, CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP);
        when(decodedJWT.getClaim("jti")).thenReturn(mock(com.auth0.jwt.interfaces.Claim.class));
        when(decodedJWT.getClaim("jti").asString()).thenReturn(refreshTokenId);
        when(refreshTokenService.findRefreshTokenByRefreshTokenId(refreshTokenId))
                .thenThrow(new RuntimeException("Refresh token not found"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(mockRefreshToken);
        });
        assertEquals("Refresh token not found", exception.getMessage());
        verify(jwtUtil).validateToken(mockRefreshToken);
        verify(jwtUtil).validateTypeToken(decodedJWT, CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP);
        verify(refreshTokenService).findRefreshTokenByRefreshTokenId(refreshTokenId);
        verifyNoMoreInteractions(accessTokenService);
    }
}