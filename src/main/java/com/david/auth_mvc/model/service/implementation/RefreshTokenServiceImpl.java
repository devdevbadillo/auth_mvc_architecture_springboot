package com.david.auth_mvc.model.service.implementation;

import com.david.auth_mvc.model.service.interfaces.IRefreshTokenService;
import com.david.auth_mvc.common.mapper.RefreshTokenEntityMapper;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.RefreshToken;
import com.david.auth_mvc.model.repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class RefreshTokenServiceImpl implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenEntityMapper refreshTokenEntityMapper;


    @Override
    public void saveRefreshTokenToAccessApp(String refreshToken, Credential credential, AccessToken accessToken) {
        RefreshToken refreshTokenEntity = this.refreshTokenEntityMapper.toRefreshTokenEntity(refreshToken, credential, CommonConstants.TYPE_REFRESH_TOKEN, accessToken);
        this.refreshTokenRepository.save(refreshTokenEntity);
    }

    @Override
    public RefreshToken findRefreshTokenByRefreshTokenId(String refreshTokenId) {
        return this.refreshTokenRepository.findRefreshTokenByRefreshTokenId(refreshTokenId);
    }

    @Override
    public RefreshToken findRefreshTokenByAccessToken(Long accessTokenId) {
        return this.refreshTokenRepository.findRefreshTokenByAccessTokenId(accessTokenId);
    }

    @Override
    public void deleteRefreshToken(AccessToken accessToken) {
        RefreshToken refreshToken = this.findRefreshTokenByAccessToken(accessToken.getId());
        this.refreshTokenRepository.delete(refreshToken);
    }
}
