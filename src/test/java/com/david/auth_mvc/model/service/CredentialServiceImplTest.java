package com.david.auth_mvc.model.service.implementation;

import com.david.auth_mvc.common.exceptions.accessToken.AlreadyHaveAccessTokenToChangePasswordException;
import com.david.auth_mvc.common.exceptions.auth.HaveAccessWithOAuth2Exception;
import com.david.auth_mvc.common.exceptions.auth.UserNotVerifiedException;
import com.david.auth_mvc.common.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.common.exceptions.credential.UserNotFoundException;
import com.david.auth_mvc.common.mapper.CredentialEntityMapper;
import com.david.auth_mvc.common.utils.JwtUtil;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.messages.AuthMessages;
import com.david.auth_mvc.common.utils.constants.messages.CredentialMessages;
import com.david.auth_mvc.model.domain.dto.request.ChangePasswordRequest;
import com.david.auth_mvc.model.domain.dto.request.RecoveryAccountRequest;
import com.david.auth_mvc.model.domain.dto.request.SignUpRequest;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.dto.response.SignInResponse;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.RefreshToken;
import com.david.auth_mvc.model.repository.CredentialRepository;
import com.david.auth_mvc.model.service.interfaces.IAccessTokenService;
import com.david.auth_mvc.model.service.interfaces.IEmailService;
import com.david.auth_mvc.model.service.interfaces.IRefreshTokenService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class CredentialServiceImplTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private CredentialEntityMapper credentialEntityMapper;

    @Mock
    private IEmailService emailService;

    @Mock
    private IAccessTokenService accessTokenService;

    @Mock
    private IRefreshTokenService refreshTokenService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private CredentialServiceImpl credentialService;

    private SignUpRequest signUpRequest;
    private Credential credential;
    private String email;
    private String accessToken;
    private String refreshToken;
    private AccessToken accessTokenEntity;

    @BeforeEach
    void setUp() {
        email = "test@example.com";
        signUpRequest = new SignUpRequest();
        signUpRequest.setEmail(email);
        signUpRequest.setPassword("password123");

        credential = new Credential();
        credential.setEmail(email);
        credential.setPassword("hashedPassword");
        credential.setIsVerified(false);
        credential.setIsAccesOauth(false);

        accessToken = "access-token-123";
        refreshToken = "refresh-token-123";

        accessTokenEntity = new AccessToken();
        accessTokenEntity.setAccessTokenId(accessToken);
        accessTokenEntity.setCredential(credential);
    }

    @Test
    @DisplayName("SignUp - Should create a new user successfully")
    void signUp_ShouldCreateNewUser() throws UserAlreadyExistException, MessagingException {
        // Arrange
        when(credentialRepository.getCredentialByEmail(email)).thenReturn(null);
        when(credentialEntityMapper.toCredentialEntity(signUpRequest)).thenReturn(credential);
        when(jwtUtil.generateAccessToken(eq(credential), anyInt(), eq(CommonConstants.TYPE_VERIFY_ACCOUNT)))
                .thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(eq(credential), anyInt(), eq(CommonConstants.TYPE_REFRESH_TOKEN_TO_VERIFY_ACCOUNT)))
                .thenReturn(refreshToken);
        when(accessTokenService.saveAccessToken(accessToken, credential, CommonConstants.TYPE_VERIFY_ACCOUNT))
                .thenReturn(accessTokenEntity);

        // Act
        MessageResponse response = credentialService.signUp(signUpRequest);

        // Assert
        assertEquals(CredentialMessages.USER_CREATED_SUCCESSFULLY, response.getMessage());
        verify(credentialRepository).save(credential);
        verify(emailService).sendEmailVerifyAccount(email, accessToken, refreshToken);
        verify(refreshTokenService).saveRefreshToken(eq(refreshToken), eq(credential), eq(accessTokenEntity),
                eq(CommonConstants.TYPE_REFRESH_TOKEN_TO_VERIFY_ACCOUNT));
    }

    @Test
    @DisplayName("SignUp - Should throw exception when user already exists")
    void signUp_ShouldThrowExceptionWhenUserExists() {
        // Arrange
        when(credentialRepository.getCredentialByEmail(email)).thenReturn(credential);

        // Act & Assert
        UserAlreadyExistException exception = assertThrows(UserAlreadyExistException.class, () -> {
            credentialService.signUp(signUpRequest);
        });
        assertEquals(CredentialMessages.USER_ALREADY_EXISTS, exception.getMessage());
        verify(credentialRepository, never()).save(any(Credential.class));
    }

    @Test
    @DisplayName("VerifyAccount - Should verify user account successfully")
    void verifyAccount_ShouldVerifyAccountSuccessfully() {
        // Arrange
        when(accessTokenService.getTokenByAccessTokenId(accessToken)).thenReturn(accessTokenEntity);
        when(jwtUtil.generateAccessToken(eq(credential), anyInt(), eq(CommonConstants.TYPE_ACCESS_TOKEN)))
                .thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(eq(credential), anyInt(), eq(CommonConstants.TYPE_REFRESH_TOKEN)))
                .thenReturn("new-refresh-token");
        when(accessTokenService.saveAccessTokenToAccessApp("new-access-token", credential))
                .thenReturn(new AccessToken());

        // Act
        SignInResponse response = credentialService.verifyAccount(accessToken);

        // Assert
        assertNotNull(response);
        assertTrue(credential.getIsVerified());
        verify(credentialRepository).save(credential);
        verify(refreshTokenService).deleteRefreshToken(accessTokenEntity);
        verify(refreshTokenService).saveRefreshToken(eq("new-refresh-token"), eq(credential), any(AccessToken.class),
                eq(CommonConstants.TYPE_REFRESH_TOKEN));
    }

    @Test
    @DisplayName("RefreshAccessToVerifyAccount - Should refresh access token successfully")
    void refreshAccessToVerifyAccount_ShouldRefreshAccessTokenSuccessfully() throws UserNotFoundException, AlreadyHaveAccessTokenToChangePasswordException, MessagingException {
        // Arrange
        when(credentialRepository.getCredentialByEmail(email)).thenReturn(credential);
        doNothing().when(accessTokenService).hasAccessToken(credential, CommonConstants.TYPE_VERIFY_ACCOUNT);
        when(jwtUtil.generateAccessToken(eq(credential), anyInt(), eq(CommonConstants.TYPE_VERIFY_ACCOUNT)))
                .thenReturn("new-access-token");
        when(accessTokenService.saveAccessToken("new-access-token", credential, CommonConstants.TYPE_VERIFY_ACCOUNT))
                .thenReturn(new AccessToken());

        // Act
        MessageResponse response = credentialService.refreshAccessToVerifyAccount(refreshToken, email);

        // Assert
        assertEquals(CredentialMessages.SEND_EMAIL_VERIFY_ACCOUNT_SUCCESSFULLY, response.getMessage());
        verify(emailService).sendEmailVerifyAccount(email, "new-access-token", refreshToken);
    }

    @Test
    @DisplayName("RefreshAccessToVerifyAccount - Should throw exception when user not found")
    void refreshAccessToVerifyAccount_ShouldThrowExceptionWhenUserNotFound() throws AlreadyHaveAccessTokenToChangePasswordException {
        // Arrange
        when(credentialRepository.getCredentialByEmail(email)).thenReturn(null);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            credentialService.refreshAccessToVerifyAccount(refreshToken, email);
        });
        assertEquals(CredentialMessages.USER_NOT_REGISTERED, exception.getMessage());
        verify(accessTokenService, never()).hasAccessToken(any(), anyString());
    }

    @Test
    @DisplayName("RecoveryAccount - Should send recovery instructions successfully")
    void recoveryAccount_ShouldSendRecoveryInstructionsSuccessfully() throws UserNotFoundException, HaveAccessWithOAuth2Exception, MessagingException, AlreadyHaveAccessTokenToChangePasswordException, UserNotVerifiedException {
        // Arrange
        RecoveryAccountRequest request = new RecoveryAccountRequest();
        request.setEmail(email);

        credential.setIsVerified(true);

        when(credentialRepository.getCredentialByEmail(email)).thenReturn(credential);
        doNothing().when(accessTokenService).hasAccessToken(credential, CommonConstants.TYPE_CHANGE_PASSWORD);
        when(jwtUtil.generateAccessToken(eq(credential), anyInt(), eq(CommonConstants.TYPE_CHANGE_PASSWORD)))
                .thenReturn("recovery-access-token");
        when(accessTokenService.saveAccessToken("recovery-access-token", credential, CommonConstants.TYPE_CHANGE_PASSWORD))
                .thenReturn(new AccessToken());

        // Act
        MessageResponse response = credentialService.recoveryAccount(request);

        // Assert
        assertEquals(CredentialMessages.RECOVERY_ACCOUNT_INSTRUCTIONS_SENT, response.getMessage());
        verify(emailService).sendEmailRecoveryAccount(email, "recovery-access-token");
    }

    @Test
    @DisplayName("RecoveryAccount - Should throw exception when user has OAuth2 access")
    void recoveryAccount_ShouldThrowExceptionWhenUserHasOAuth2Access() throws AlreadyHaveAccessTokenToChangePasswordException {
        // Arrange
        RecoveryAccountRequest request = new RecoveryAccountRequest();
        request.setEmail(email);

        credential.setIsVerified(true);
        credential.setIsAccesOauth(true);

        when(credentialRepository.getCredentialByEmail(email)).thenReturn(credential);

        // Act & Assert
        HaveAccessWithOAuth2Exception exception = assertThrows(HaveAccessWithOAuth2Exception.class, () -> {
            credentialService.recoveryAccount(request);
        });
        assertEquals(AuthMessages.ACCESS_WITH_OAUTH2_ERROR, exception.getMessage());
        verify(accessTokenService, never()).hasAccessToken(any(), anyString());
    }

    @Test
    @DisplayName("RecoveryAccount - Should throw exception when user is not verified")
    void recoveryAccount_ShouldThrowExceptionWhenUserIsNotVerified() {
        // Arrange
        RecoveryAccountRequest request = new RecoveryAccountRequest();
        request.setEmail(email);

        credential.setIsVerified(false);

        when(credentialRepository.getCredentialByEmail(email)).thenReturn(credential);

        // Act & Assert
        UserNotVerifiedException exception = assertThrows(UserNotVerifiedException.class, () -> {
            credentialService.recoveryAccount(request);
        });
        assertEquals(AuthMessages.USER_NOT_VERIFIED_ERROR, exception.getMessage());
    }

    @Test
    @DisplayName("ChangePassword - Should change password successfully")
    void changePassword_ShouldChangePasswordSuccessfully() throws UserNotFoundException, HaveAccessWithOAuth2Exception {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("newPassword123");

        String encodedPassword = "encodedNewPassword";

        when(credentialRepository.getCredentialByEmail(email)).thenReturn(credential);
        when(credentialEntityMapper.encodePassword(request.getPassword())).thenReturn(encodedPassword);
        doNothing().when(accessTokenService).deleteAccessToken(accessToken);

        // Act
        MessageResponse response = credentialService.changePassword(request, email, accessToken);

        // Assert
        assertEquals(CredentialMessages.CHANGE_PASSWORD_SUCCESSFULLY, response.getMessage());
        assertEquals(encodedPassword, credential.getPassword());
        verify(credentialRepository).save(credential);
        verify(accessTokenService).deleteAccessToken(accessToken);
    }

    @Test
    @DisplayName("ChangePassword - Should throw exception when user has OAuth2 access")
    void changePassword_ShouldThrowExceptionWhenUserHasOAuth2Access() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("newPassword123");

        credential.setIsAccesOauth(true);

        when(credentialRepository.getCredentialByEmail(email)).thenReturn(credential);

        // Act & Assert
        HaveAccessWithOAuth2Exception exception = assertThrows(HaveAccessWithOAuth2Exception.class, () -> {
            credentialService.changePassword(request, email, accessToken);
        });
        assertEquals(AuthMessages.ACCESS_WITH_OAUTH2_ERROR, exception.getMessage());
        verify(credentialEntityMapper, never()).encodePassword(anyString());
        verify(credentialRepository, never()).save(any(Credential.class));
    }

    @Test
    @DisplayName("IsRegisteredUser - Should return credential when user exists")
    void isRegisteredUser_ShouldReturnCredentialWhenUserExists() throws UserNotFoundException {
        // Arrange
        when(credentialRepository.getCredentialByEmail(email)).thenReturn(credential);

        // Act
        Credential result = credentialService.isRegisteredUser(email);

        // Assert
        assertNotNull(result);
        assertEquals(credential, result);
    }

    @Test
    @DisplayName("IsRegisteredUser - Should throw exception when user does not exist")
    void isRegisteredUser_ShouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        when(credentialRepository.getCredentialByEmail(email)).thenReturn(null);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            credentialService.isRegisteredUser(email);
        });
        assertEquals(CredentialMessages.USER_NOT_REGISTERED, exception.getMessage());
    }

    @Test
    @DisplayName("HasAccessWithOAuth2 - Should not throw exception when user does not have OAuth2 access")
    void hasAccessWithOAuth2_ShouldNotThrowExceptionWhenUserDoesNotHaveOAuth2Access() throws HaveAccessWithOAuth2Exception {
        // Arrange
        credential.setIsAccesOauth(false);

        // Act & Assert
        assertDoesNotThrow(() -> {
            credentialService.hasAccessWithOAuth2(credential);
        });
    }

    @Test
    @DisplayName("HasAccessWithOAuth2 - Should throw exception when user has OAuth2 access")
    void hasAccessWithOAuth2_ShouldThrowExceptionWhenUserHasOAuth2Access() {
        // Arrange
        credential.setIsAccesOauth(true);

        // Act & Assert
        HaveAccessWithOAuth2Exception exception = assertThrows(HaveAccessWithOAuth2Exception.class, () -> {
            credentialService.hasAccessWithOAuth2(credential);
        });
        assertEquals(AuthMessages.ACCESS_WITH_OAUTH2_ERROR, exception.getMessage());
    }

    @Test
    @DisplayName("SignUp Entity - Should create a new user successfully")
    void signUpEntity_ShouldCreateNewUser() throws UserAlreadyExistException {
        // Arrange
        when(credentialRepository.getCredentialByEmail(email)).thenReturn(null);

        // Act
        credentialService.signUp(credential);

        // Assert
        verify(credentialRepository).save(credential);
    }

    @Test
    @DisplayName("SignUp Entity - Should throw exception when user already exists")
    void signUpEntity_ShouldThrowExceptionWhenUserExists() {
        // Arrange
        when(credentialRepository.getCredentialByEmail(email)).thenReturn(credential);

        // Act & Assert
        UserAlreadyExistException exception = assertThrows(UserAlreadyExistException.class, () -> {
            credentialService.signUp(credential);
        });
        assertEquals(CredentialMessages.USER_ALREADY_EXISTS, exception.getMessage());
        verify(credentialRepository, never()).save(any(Credential.class));
    }
}