package com.david.auth_mvc.model.services.application;

import com.david.auth_mvc.model.domain.services.IAccessTokenService;
import com.david.auth_mvc.model.domain.services.IRefreshTokenService;
import com.david.auth_mvc.model.domain.services.application.IUserService;
import com.david.auth_mvc.common.utils.constants.messages.UserMessages;
import com.david.auth_mvc.model.domain.dto.response.MessageResponse;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements IUserService {

    private final IAccessTokenService accessTokenService;
    private final IRefreshTokenService refreshTokenService;

    @Override
    public MessageResponse signOut(String accessTokenId) {
        AccessToken accessToken = this.accessTokenService.getTokenByAccessTokenId(accessTokenId);

        this.refreshTokenService.deleteRefreshToken(accessToken);
        return new MessageResponse(UserMessages.SIGN_OUT_SUCCESSFULLY);
    }
}
