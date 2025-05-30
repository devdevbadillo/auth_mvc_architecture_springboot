package com.david.auth_mvc.model.services;

import com.david.auth_mvc.controller.messages.UserMessages;
import com.david.auth_mvc.controller.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.business.services.impl.application.UserServiceImpl;
import com.david.auth_mvc.model.business.services.interfaces.IAccessTokenService;
import com.david.auth_mvc.model.business.services.interfaces.IRefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private IAccessTokenService accessTokenService;

    @Mock
    private IRefreshTokenService refreshTokenService;

    @InjectMocks
    private UserServiceImpl userService;

    private AccessToken mockAccessToken;
    private final String accessTokenId = "test-token-id";

    @BeforeEach
    void setUp() {
        mockAccessToken = new AccessToken();
        mockAccessToken.setAccessTokenId(accessTokenId);
    }

    @Test
    void signOut_ShouldDeleteRefreshTokenAndReturnSuccessMessage() {
        // Arrange
        when(accessTokenService.getTokenByAccessTokenId(accessTokenId)).thenReturn(mockAccessToken);
        doNothing().when(refreshTokenService).deleteRefreshToken(mockAccessToken);

        // Act
        MessageResponse response = userService.signOut(accessTokenId);

        // Assert
        verify(accessTokenService, times(1)).getTokenByAccessTokenId(accessTokenId);
        verify(refreshTokenService, times(1)).deleteRefreshToken(mockAccessToken);

        assertNotNull(response);
        assertEquals(UserMessages.SIGN_OUT_SUCCESSFULLY, response.getMessage());
    }


    @Test
    void signOut_ShouldHandleTokenNotFound() {
        // Arrange
        when(accessTokenService.getTokenByAccessTokenId("non-existent-token")).thenReturn(null);

        // Act
        MessageResponse response = userService.signOut("non-existent-token");

        // Assert
        assertNotNull(response);
        verify(accessTokenService, times(1)).getTokenByAccessTokenId("non-existent-token");
    }
}