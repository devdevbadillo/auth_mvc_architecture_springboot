package com.david.auth_mvc.model.service.interfaces;

import com.david.auth_mvc.common.exceptions.accessToken.AlreadyHaveAccessTokenToChangePasswordException;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;

public interface IAccessTokenService {

    void hasAccessTokenToChangePassword(Credential credential) throws AlreadyHaveAccessTokenToChangePasswordException;

    void saveAccessTokenToChangePassword(String accessToken, Credential credential);

    AccessToken saveAccessTokenToAccessApp(String accessToken, Credential credential);

    AccessToken saveAccessTokenToAccessAppWithRefreshToken(AccessToken oldAccessToken, String accessToken);

    AccessToken getTokenByAccessTokenId(String accessTokenId);

    void deleteAccessToken(String accessTokenId);
}
