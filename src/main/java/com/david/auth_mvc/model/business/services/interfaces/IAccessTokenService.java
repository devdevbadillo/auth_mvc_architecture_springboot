package com.david.auth_mvc.model.business.services.interfaces;

import com.david.auth_mvc.model.domain.exceptions.accessToken.AlreadyHaveAccessTokenToChangePasswordException;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;

public interface IAccessTokenService {

    void hasAccessToken(Credential credential, String typeToken) throws AlreadyHaveAccessTokenToChangePasswordException;

    AccessToken saveAccessToken(String accessToken, Credential credential, String typeToken);

    AccessToken saveAccessTokenToAccessApp(String accessToken, Credential credential);

    void saveAccessTokenToAccessAppWithRefreshToken(AccessToken oldAccessToken, String accessToken);

    AccessToken getTokenByAccessTokenId(String accessTokenId);

    void deleteAccessToken(String accessTokenId);
}
