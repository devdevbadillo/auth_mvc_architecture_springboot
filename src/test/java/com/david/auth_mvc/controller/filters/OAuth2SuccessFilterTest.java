package com.david.auth_mvc.controller.filters;

import com.david.auth_mvc.model.domain.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.model.infrestructure.utils.JwtUtil;
import com.david.auth_mvc.model.infrestructure.utils.constants.CommonConstants;
import com.david.auth_mvc.controller.messages.AuthMessages;
import com.david.auth_mvc.model.infrestructure.filters.auth.OAuth2SuccessFilter;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.infrestructure.repository.CredentialRepository;
import com.david.auth_mvc.model.business.services.interfaces.IAccessTokenService;
import com.david.auth_mvc.model.business.services.interfaces.application.ICredentialService;
import com.david.auth_mvc.model.business.services.interfaces.IRefreshTokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuth2SuccessFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ICredentialService credentialService;

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private IRefreshTokenService refreshTokenService;

    @Mock
    private IAccessTokenService accessTokenService;

    @Mock
    private Authentication authentication;

    @Mock
    private DefaultOAuth2User oAuth2User;

    @Mock
    private RedirectStrategy redirectStrategy;

    @InjectMocks
    private OAuth2SuccessFilter oAuth2SuccessFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Date mockAccessTokenExpiration;
    private Date mockRefreshTokenExpiration;
    private Date mockErrorTokenExpiration;
    private final String testEmail = "test@example.com";
    private final String mockAccessToken = "mock-access-token";
    private final String mockRefreshToken = "mock-refresh-token";
    private final String mockErrorToken = "mock-error-token";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        mockAccessTokenExpiration = new Date(System.currentTimeMillis() + 3600000); // 1 hour in future
        mockRefreshTokenExpiration = new Date(System.currentTimeMillis() + 86400000); // 1 day in future
        mockErrorTokenExpiration = new Date(System.currentTimeMillis() + 60000); // 1 minute in future

        // Set a redirect strategy that we can test
        oAuth2SuccessFilter.setRedirectStrategy(redirectStrategy);
    }

    @Test
    void onAuthenticationSuccess_WithValidEmail_AndNewUser_ShouldRegisterAndRedirect() throws IOException, UserAlreadyExistException {
        // Arrange
        setupMockAuthentication(testEmail);
        when(jwtUtil.calculateExpirationMinutesToken(CommonConstants.EXPIRATION_TOKEN_TO_ACCESS_APP)).thenReturn(mockAccessTokenExpiration);
        when(jwtUtil.calculateExpirationDaysToken(CommonConstants.EXPIRATION_REFRESH_TOKEN_TO_ACCESS_APP)).thenReturn(mockRefreshTokenExpiration);
        when(jwtUtil.generateToken(eq(testEmail), eq(mockAccessTokenExpiration), eq(CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP))).thenReturn(mockAccessToken);
        when(jwtUtil.generateToken(eq(testEmail), eq(mockRefreshTokenExpiration), eq(CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP))).thenReturn(mockRefreshToken);

        // Create a credential that would be returned
        Credential returnedCredential = createTestCredential(true);

        // Mock the service behavior for a new user
        doNothing().when(credentialService).signUp(any(Credential.class));
        when(credentialRepository.getCredentialByEmail(testEmail)).thenReturn(returnedCredential);

        AccessToken mockAccessTokenEntity = new AccessToken();
        when(accessTokenService.saveAccessTokenToAccessApp(eq(mockAccessToken), any(Credential.class))).thenReturn(mockAccessTokenEntity);

        // Act
        oAuth2SuccessFilter.onAuthenticationSuccess(request, response, authentication);

        // Assert
        String expectedRedirectUrl = String.format("%s?accessToken=%s&refreshToken=%s",
                CommonConstants.AUTH_SOCIAL_MEDIA_FRONT_URL, mockAccessToken, mockRefreshToken);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), eq(expectedRedirectUrl));
        verify(refreshTokenService).saveRefreshToken(
                eq(mockRefreshToken),
                eq(returnedCredential),
                eq(mockAccessTokenEntity),
                eq(CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP));
    }

    @Test
    void onAuthenticationSuccess_WithValidEmail_AndExistingOAuthUser_ShouldRedirect() throws IOException, UserAlreadyExistException {
        // Arrange
        setupMockAuthentication(testEmail);
        when(jwtUtil.calculateExpirationMinutesToken(CommonConstants.EXPIRATION_TOKEN_TO_ACCESS_APP)).thenReturn(mockAccessTokenExpiration);
        when(jwtUtil.calculateExpirationDaysToken(CommonConstants.EXPIRATION_REFRESH_TOKEN_TO_ACCESS_APP)).thenReturn(mockRefreshTokenExpiration);
        when(jwtUtil.generateToken(eq(testEmail), eq(mockAccessTokenExpiration), eq(CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP))).thenReturn(mockAccessToken);
        when(jwtUtil.generateToken(eq(testEmail), eq(mockRefreshTokenExpiration), eq(CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP))).thenReturn(mockRefreshToken);

        doThrow(new UserAlreadyExistException("User already exists")).when(credentialService).signUp(any(Credential.class));

        // Create a credential that would be returned as an existing OAuth user
        Credential existingCredential = createTestCredential(true);
        when(credentialRepository.getCredentialByEmail(testEmail)).thenReturn(existingCredential);

        AccessToken mockAccessTokenEntity = new AccessToken();
        when(accessTokenService.saveAccessTokenToAccessApp(eq(mockAccessToken), any(Credential.class))).thenReturn(mockAccessTokenEntity);

        // Act
        oAuth2SuccessFilter.onAuthenticationSuccess(request, response, authentication);

        // Assert
        String expectedRedirectUrl = String.format("%s?accessToken=%s&refreshToken=%s",
                CommonConstants.AUTH_SOCIAL_MEDIA_FRONT_URL, mockAccessToken, mockRefreshToken);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), eq(expectedRedirectUrl));
        verify(refreshTokenService).saveRefreshToken(
                eq(mockRefreshToken),
                eq(existingCredential),
                eq(mockAccessTokenEntity),
                eq(CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP));
    }

    @Test
    void onAuthenticationSuccess_WithValidEmail_AndExistingNonOAuthUser_ShouldRedirectWithError() throws IOException, UserAlreadyExistException {
        // Arrange
        setupMockAuthentication(testEmail);
        when(jwtUtil.calculateExpirationMinutesToken(CommonConstants.EXPIRATION_TOKEN_TO_ACCESS_APP)).thenReturn(mockAccessTokenExpiration);
        when(jwtUtil.calculateExpirationDaysToken(CommonConstants.EXPIRATION_REFRESH_TOKEN_TO_ACCESS_APP)).thenReturn(mockRefreshTokenExpiration);
        when(jwtUtil.calculateExpirationSecondsToken(CommonConstants.EXPIRATION_ERROR_TOKEN)).thenReturn(mockErrorTokenExpiration);
        when(jwtUtil.generateToken(eq(mockErrorTokenExpiration), eq(CommonConstants.TYPE_ERROR_TOKEN))).thenReturn(mockErrorToken);

        doThrow(new UserAlreadyExistException("User already exists")).when(credentialService).signUp(any(Credential.class));

        // Create a credential that would be returned as an existing non-OAuth user
        Credential existingCredential = createTestCredential(false);
        when(credentialRepository.getCredentialByEmail(testEmail)).thenReturn(existingCredential);

        // Act
        oAuth2SuccessFilter.onAuthenticationSuccess(request, response, authentication);

        // Assert
        String encodedErrorMessage = URLEncoder.encode(AuthMessages.ACCESS_WITH_OAUTH2_ERROR, StandardCharsets.UTF_8);
        String expectedRedirectUrl = String.format("%s?error=%s&errorToken=%s",
                CommonConstants.AUTH_SOCIAL_MEDIA_FRONT_URL, encodedErrorMessage, mockErrorToken);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), eq(expectedRedirectUrl));
    }

    @Test
    void onAuthenticationSuccess_WithNullEmail_ShouldRedirectWithError() throws IOException {
        // Arrange
        setupMockAuthentication(null);
        when(jwtUtil.calculateExpirationSecondsToken(CommonConstants.EXPIRATION_ERROR_TOKEN)).thenReturn(mockErrorTokenExpiration);
        when(jwtUtil.generateToken(mockErrorTokenExpiration, CommonConstants.TYPE_ERROR_TOKEN)).thenReturn(mockErrorToken);

        // Act
        oAuth2SuccessFilter.onAuthenticationSuccess(request, response, authentication);

        // Assert
        String encodedErrorMessage = URLEncoder.encode(AuthMessages.OAUTH2_EMAIL_NULL_OR_INVALID_ERROR, StandardCharsets.UTF_8);
        String expectedRedirectUrl = String.format("%s?error=%s&errorToken=%s",
                CommonConstants.AUTH_SOCIAL_MEDIA_FRONT_URL, encodedErrorMessage, mockErrorToken);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), eq(expectedRedirectUrl));
    }

    @Test
    void onAuthenticationSuccess_WithEmptyEmail_ShouldRedirectWithError() throws IOException {
        // Arrange
        setupMockAuthentication("");
        when(jwtUtil.calculateExpirationSecondsToken(CommonConstants.EXPIRATION_ERROR_TOKEN)).thenReturn(mockErrorTokenExpiration);
        when(jwtUtil.generateToken(mockErrorTokenExpiration, CommonConstants.TYPE_ERROR_TOKEN)).thenReturn(mockErrorToken);

        // Act
        oAuth2SuccessFilter.onAuthenticationSuccess(request, response, authentication);

        // Assert
        String encodedErrorMessage = URLEncoder.encode(AuthMessages.OAUTH2_EMAIL_NULL_OR_INVALID_ERROR, StandardCharsets.UTF_8);
        String expectedRedirectUrl = String.format("%s?error=%s&errorToken=%s",
                CommonConstants.AUTH_SOCIAL_MEDIA_FRONT_URL, encodedErrorMessage, mockErrorToken);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), eq(expectedRedirectUrl));
    }

    @Test
    void testSignUpNewUser() throws UserAlreadyExistException, IOException {
        // Arrange
        setupMockAuthentication(testEmail);
        when(jwtUtil.calculateExpirationMinutesToken(CommonConstants.EXPIRATION_TOKEN_TO_ACCESS_APP)).thenReturn(mockAccessTokenExpiration);
        when(jwtUtil.calculateExpirationDaysToken(CommonConstants.EXPIRATION_REFRESH_TOKEN_TO_ACCESS_APP)).thenReturn(mockRefreshTokenExpiration);
        when(jwtUtil.generateToken(eq(testEmail), eq(mockAccessTokenExpiration), eq(CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP))).thenReturn(mockAccessToken);
        when(jwtUtil.generateToken(eq(testEmail), eq(mockRefreshTokenExpiration), eq(CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP))).thenReturn(mockRefreshToken);

        // Capture the credential being passed to signUp
        ArgumentCaptor<Credential> credentialCaptor = ArgumentCaptor.forClass(Credential.class);
        doNothing().when(credentialService).signUp(credentialCaptor.capture());

        // Mock return of credential after signup
        Credential returnedCredential = createTestCredential(true);
        when(credentialRepository.getCredentialByEmail(testEmail)).thenReturn(returnedCredential);

        AccessToken mockAccessTokenEntity = new AccessToken();
        when(accessTokenService.saveAccessTokenToAccessApp(eq(mockAccessToken), any(Credential.class))).thenReturn(mockAccessTokenEntity);

        // Act
        oAuth2SuccessFilter.onAuthenticationSuccess(request, response, authentication);

        // Assert
        Credential capturedCredential = credentialCaptor.getValue();

        assertEquals(testEmail, capturedCredential.getEmail());
        assertTrue(capturedCredential.getIsAccesOauth());
        assertTrue(capturedCredential.getIsVerified());
    }

    // Helper methods
    private void setupMockAuthentication(String email) {

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn("Test user");
    }

    private Credential createTestCredential( boolean isOAuth) {
        return Credential.builder()
                .email(testEmail)
                .name("Test user")
                .isAccesOauth(isOAuth)
                .isVerified(true)
                .build();
    }
}