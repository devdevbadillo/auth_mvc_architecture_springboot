package com.david.auth_mvc.model.service.interfaces;

import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.RefreshToken;

public interface IRefreshTokenService {

    void saveRefreshTokenToAccessApp(String refreshToken, Credential credential, AccessToken accessToken);

    RefreshToken findRefreshTokenByRefreshTokenId(String refreshTokenId);

    RefreshToken findRefreshTokenByAccessToken(Long accessTokenId);

    void deleteRefreshToken(AccessToken accessToken);

}
