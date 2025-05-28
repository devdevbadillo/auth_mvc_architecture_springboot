package com.david.auth_mvc.model.services;

import com.david.auth_mvc.model.domain.services.ITypeTokenService;
import com.david.auth_mvc.common.exceptions.accessToken.AlreadyHaveAccessTokenToChangePasswordException;
import com.david.auth_mvc.common.mapper.AccessTokenEntityMapper;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.messages.CredentialMessages;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.TypeToken;
import com.david.auth_mvc.model.infrestructure.repository.AccessTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccessTokenServiceImplTest {

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private ITypeTokenService typeTokenService;

    @Mock
    private AccessTokenEntityMapper accessTokenEntityMapper;

    @InjectMocks
    private AccessTokenServiceImpl accessTokenService;

    private Credential credential;
    private TypeToken typeToken;
    private AccessToken accessToken;
    private AccessToken newAccessToken;
    private String accessTokenId;
    private String typeTokenName;

    @BeforeEach
    void setUp() {
        credential = new Credential();
        typeToken = new TypeToken();
        typeTokenName = "CHANGE_PASSWORD";

        // Mock actual date
        Date currentDate = new Date();

        // Create a future date (1 hour from now)
        Date futureDate = new Date(currentDate.getTime() + 3600000);

        // Create a past date (1 hour ago)
        Date pastDate = new Date(currentDate.getTime() - 3600000);

        accessToken = new AccessToken();
        accessToken.setAccessTokenId("token123");
        accessToken.setCredential(credential);
        accessToken.setTypeToken(typeToken);
        accessToken.setCreationDate(currentDate);
        accessToken.setExpirationDate(futureDate);

        newAccessToken = new AccessToken();
        newAccessToken.setAccessTokenId("newToken456");
        newAccessToken.setCredential(credential);
        newAccessToken.setTypeToken(typeToken);
        newAccessToken.setCreationDate(currentDate);
        newAccessToken.setExpirationDate(futureDate);

        accessTokenId = "token123";
    }

    @Test
    void hasAccessToken_WithValidNotExpiredToken_ShouldThrowException() {
        // Given
        when(typeTokenService.getTypeToken(typeTokenName)).thenReturn(typeToken);
        when(accessTokenRepository.getTokenByCredentialAndTypeToken(credential, typeToken)).thenReturn(accessToken);

        // When & Then
        AlreadyHaveAccessTokenToChangePasswordException exception = assertThrows(
                AlreadyHaveAccessTokenToChangePasswordException.class,
                () -> accessTokenService.hasAccessToken(credential, typeTokenName)
        );

        assertEquals(CredentialMessages.ALREADY_HAVE_ACCESS_TOKEN_TO_CHANGE_PASSWORD, exception.getMessage());
        verify(typeTokenService).getTypeToken(typeTokenName);
        verify(accessTokenRepository).getTokenByCredentialAndTypeToken(credential, typeToken);
    }

    @Test
    void hasAccessToken_WithExpiredToken_ShouldNotThrowException() {
        // Given
        AccessToken expiredToken = new AccessToken();
        Date pastDate = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago
        expiredToken.setExpirationDate(pastDate);

        when(typeTokenService.getTypeToken(typeTokenName)).thenReturn(typeToken);
        when(accessTokenRepository.getTokenByCredentialAndTypeToken(credential, typeToken)).thenReturn(expiredToken);

        // When & Then
        assertDoesNotThrow(() -> accessTokenService.hasAccessToken(credential, typeTokenName));

        verify(typeTokenService).getTypeToken(typeTokenName);
        verify(accessTokenRepository).getTokenByCredentialAndTypeToken(credential, typeToken);
    }

    @Test
    void hasAccessToken_WithNoToken_ShouldNotThrowException() {
        // Given
        when(typeTokenService.getTypeToken(typeTokenName)).thenReturn(typeToken);
        when(accessTokenRepository.getTokenByCredentialAndTypeToken(credential, typeToken)).thenReturn(null);

        // When & Then
        assertDoesNotThrow(() -> accessTokenService.hasAccessToken(credential, typeTokenName));

        verify(typeTokenService).getTypeToken(typeTokenName);
        verify(accessTokenRepository).getTokenByCredentialAndTypeToken(credential, typeToken);
    }

    @Test
    void saveAccessToken_WithNoExistingToken_ShouldSaveNewToken() {
        // Given
        String tokenValue = "newToken456";

        when(typeTokenService.getTypeToken(typeTokenName)).thenReturn(typeToken);
        when(accessTokenRepository.getTokenByCredentialAndTypeToken(credential, typeToken)).thenReturn(null);
        when(accessTokenEntityMapper.toTokenEntity(tokenValue, credential, typeTokenName)).thenReturn(newAccessToken);
        when(accessTokenRepository.save(newAccessToken)).thenReturn(newAccessToken);

        // When
        AccessToken result = accessTokenService.saveAccessToken(tokenValue, credential, typeTokenName);

        // Then
        assertSame(newAccessToken, result);
        verify(typeTokenService).getTypeToken(typeTokenName);
        verify(accessTokenRepository).getTokenByCredentialAndTypeToken(credential, typeToken);
        verify(accessTokenEntityMapper).toTokenEntity(tokenValue, credential, typeTokenName);
        verify(accessTokenRepository).save(newAccessToken);
    }

    @Test
    void saveAccessToken_WithExistingToken_ShouldUpdateAndSaveToken() {
        // Given
        String tokenValue = "newToken456";

        when(typeTokenService.getTypeToken(typeTokenName)).thenReturn(typeToken);
        when(accessTokenRepository.getTokenByCredentialAndTypeToken(credential, typeToken)).thenReturn(accessToken);
        when(accessTokenEntityMapper.toTokenEntity(tokenValue, credential, typeTokenName)).thenReturn(newAccessToken);
        when(accessTokenRepository.save(accessToken)).thenReturn(accessToken);

        // When
        AccessToken result = accessTokenService.saveAccessToken(tokenValue, credential, typeTokenName);

        // Then
        assertSame(accessToken, result);
        assertEquals(newAccessToken.getAccessTokenId(), accessToken.getAccessTokenId());
        assertEquals(newAccessToken.getCreationDate(), accessToken.getCreationDate());
        assertEquals(newAccessToken.getExpirationDate(), accessToken.getExpirationDate());

        verify(typeTokenService).getTypeToken(typeTokenName);
        verify(accessTokenRepository).getTokenByCredentialAndTypeToken(credential, typeToken);
        verify(accessTokenEntityMapper).toTokenEntity(tokenValue, credential, typeTokenName);
        verify(accessTokenRepository).save(accessToken);
    }

    @Test
    void saveAccessTokenToAccessApp_ShouldSaveNewToken() {
        // Given
        String tokenValue = "appToken789";

        when(accessTokenEntityMapper.toTokenEntity(tokenValue, credential, CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP))
                .thenReturn(newAccessToken);
        when(accessTokenRepository.save(newAccessToken)).thenReturn(newAccessToken);

        // When
        AccessToken result = accessTokenService.saveAccessTokenToAccessApp(tokenValue, credential);

        // Then
        assertSame(newAccessToken, result);
        verify(accessTokenEntityMapper).toTokenEntity(tokenValue, credential, CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP);
        verify(accessTokenRepository).save(newAccessToken);
    }

    @Test
    void saveAccessTokenToAccessAppWithRefreshToken_ShouldUpdateAndSaveToken() {
        // Given
        String tokenValue = "refreshedToken789";

        when(accessTokenEntityMapper.toTokenEntity(tokenValue, credential, CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP))
                .thenReturn(newAccessToken);

        when(accessTokenRepository.save(accessToken)).thenReturn(accessToken);

        // When
        accessTokenService.saveAccessTokenToAccessAppWithRefreshToken(accessToken, tokenValue);

        // Then
        assertEquals(newAccessToken.getAccessTokenId(), accessToken.getAccessTokenId());
        assertEquals(newAccessToken.getCreationDate(), accessToken.getCreationDate());
        assertEquals(newAccessToken.getExpirationDate(), accessToken.getExpirationDate());

        verify(accessTokenEntityMapper).toTokenEntity(tokenValue, credential, CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP);
        verify(accessTokenRepository).save(accessToken);
    }

    @Test
    void getTokenByAccessTokenId_ShouldReturnToken() {
        // Given
        when(accessTokenRepository.getTokenByAccessTokenId(accessTokenId)).thenReturn(accessToken);

        // When
        AccessToken result = accessTokenService.getTokenByAccessTokenId(accessTokenId);

        // Then
        assertSame(accessToken, result);
        verify(accessTokenRepository).getTokenByAccessTokenId(accessTokenId);
    }

    @Test
    void deleteAccessToken_ShouldDeleteToken() {
        // Given
        when(accessTokenRepository.getTokenByAccessTokenId(accessTokenId)).thenReturn(accessToken);

        // When
        accessTokenService.deleteAccessToken(accessTokenId);

        // Then
        verify(accessTokenRepository).getTokenByAccessTokenId(accessTokenId);
        verify(accessTokenRepository).delete(accessToken);
    }
}