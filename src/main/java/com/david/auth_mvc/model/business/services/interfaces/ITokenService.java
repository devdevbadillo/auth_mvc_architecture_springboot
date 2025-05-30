package com.david.auth_mvc.model.business.services.interfaces;

import com.david.auth_mvc.controller.dto.response.PairTokenResponse;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;

public interface ITokenService {
    PairTokenResponse generateAuthTokens(Credential credential);

    PairTokenResponse generateVerifyAccountTokens(Credential credential);

    String saveAccessToken(Credential credential, String type, Integer expiration);

    String refreshAccessTokenToAccessApp(String refreshToken);

    void revokePairTokens(String accessTokenId);

    void revokeAccessToken(String accessTokenId);

    AccessToken getAccessToken(String accessTokenId);
}
