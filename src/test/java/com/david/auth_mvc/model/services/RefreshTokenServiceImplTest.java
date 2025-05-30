package com.david.auth_mvc.model.services;

import com.david.auth_mvc.model.business.services.impl.RefreshTokenServiceImpl;
import com.david.auth_mvc.model.infrestructure.mapper.RefreshTokenEntityMapper;
import com.david.auth_mvc.model.infrestructure.utils.constants.CommonConstants;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.RefreshToken;
import com.david.auth_mvc.model.domain.entity.TypeToken;
import com.david.auth_mvc.model.infrestructure.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenEntityMapper refreshTokenEntityMapper;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private RefreshToken mockRefreshToken;
    private AccessToken mockAccessToken;
    private Credential mockCredential;
    private String mockRefreshTokenId;
    private String mockTypeToken;

    @BeforeEach
    void setUp() {
        mockRefreshTokenId = "refresh-token-123";
        mockTypeToken = CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP;

        TypeToken typeToken = new TypeToken();
        typeToken.setId(1L);
        typeToken.setType(CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP);

        mockCredential = new Credential();
        mockCredential.setId(1L);
        mockCredential.setIsVerified(true);
        mockCredential.setPassword("password");
        mockCredential.setName("test");
        mockCredential.setIsAccesOauth(false);
        mockCredential.setEmail("test@user.com");

        mockAccessToken = new AccessToken();
        mockAccessToken.setId(1L);
        mockAccessToken.setAccessTokenId("access-token-123");

        mockRefreshToken = new RefreshToken();
        mockRefreshToken.setId(1L);
        mockRefreshToken.setRefreshTokenId(mockRefreshTokenId);
        mockRefreshToken.setCredential(mockCredential);
        mockRefreshToken.setAccessToken(mockAccessToken);
        mockRefreshToken.setTypeToken(typeToken);
    }

    @Test
    void saveRefreshToken_ShouldSaveRefreshTokenSuccessfully() {
        // Arrange
        when(refreshTokenEntityMapper.toRefreshTokenEntity(mockRefreshTokenId, mockCredential, mockTypeToken, mockAccessToken))
                .thenReturn(mockRefreshToken);

        // Act
        refreshTokenService.saveRefreshToken(mockRefreshTokenId, mockCredential, mockAccessToken, mockTypeToken);

        // Assert
        verify(refreshTokenEntityMapper).toRefreshTokenEntity(mockRefreshTokenId, mockCredential, mockTypeToken, mockAccessToken);
        verify(refreshTokenRepository).save(mockRefreshToken);
    }

    @Test
    void findRefreshTokenByRefreshTokenId_ShouldReturnRefreshToken() {
        // Arrange
        when(refreshTokenRepository.findRefreshTokenByRefreshTokenId(mockRefreshTokenId)).thenReturn(mockRefreshToken);

        // Act
        RefreshToken result = refreshTokenService.findRefreshTokenByRefreshTokenId(mockRefreshTokenId);

        // Assert
        assertNotNull(result);
        assertEquals(mockRefreshToken, result);
        verify(refreshTokenRepository).findRefreshTokenByRefreshTokenId(mockRefreshTokenId);
    }

    @Test
    void findRefreshTokenByRefreshTokenId_WhenNotFound_ShouldReturnNull() {
        // Arrange
        String nonExistentTokenId = "non-existent-token";
        when(refreshTokenRepository.findRefreshTokenByRefreshTokenId(nonExistentTokenId)).thenReturn(null);

        // Act
        RefreshToken result = refreshTokenService.findRefreshTokenByRefreshTokenId(nonExistentTokenId);

        // Assert
        assertNull(result);
        verify(refreshTokenRepository).findRefreshTokenByRefreshTokenId(nonExistentTokenId);
    }

    @Test
    void findRefreshTokenByAccessToken_ShouldReturnRefreshToken() {
        // Arrange
        Long accessTokenId = 1L;
        when(refreshTokenRepository.findRefreshTokenByAccessTokenId(accessTokenId)).thenReturn(mockRefreshToken);

        // Act
        RefreshToken result = refreshTokenService.findRefreshTokenByAccessToken(accessTokenId);

        // Assert
        assertNotNull(result);
        assertEquals(mockRefreshToken, result);
        verify(refreshTokenRepository).findRefreshTokenByAccessTokenId(accessTokenId);
    }

    @Test
    void findRefreshTokenByAccessToken_WhenNotFound_ShouldReturnNull() {
        // Arrange
        Long nonExistentAccessTokenId = 999L;
        when(refreshTokenRepository.findRefreshTokenByAccessTokenId(nonExistentAccessTokenId)).thenReturn(null);

        // Act
        RefreshToken result = refreshTokenService.findRefreshTokenByAccessToken(nonExistentAccessTokenId);

        // Assert
        assertNull(result);
        verify(refreshTokenRepository).findRefreshTokenByAccessTokenId(nonExistentAccessTokenId);
    }

    @Test
    void deleteRefreshToken_ShouldDeleteRefreshTokenSuccessfully() {
        // Arrange
        when(refreshTokenRepository.findRefreshTokenByAccessTokenId(mockAccessToken.getId())).thenReturn(mockRefreshToken);

        // Act
        refreshTokenService.deleteRefreshToken(mockAccessToken);

        // Assert
        verify(refreshTokenRepository).findRefreshTokenByAccessTokenId(mockAccessToken.getId());
        verify(refreshTokenRepository).delete(mockRefreshToken);
    }

    @Test
    void deleteRefreshToken_WhenRefreshTokenNotFound_ShouldNotCallDelete() {
        // Arrange
        AccessToken nonExistentAccessToken = new AccessToken();
        nonExistentAccessToken.setId(999L);

        when(refreshTokenRepository.findRefreshTokenByAccessTokenId(nonExistentAccessToken.getId())).thenReturn(null);

        // Act
        refreshTokenService.deleteRefreshToken(nonExistentAccessToken);

        // Assert
        verify(refreshTokenRepository).findRefreshTokenByAccessTokenId(nonExistentAccessToken.getId());
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }
}