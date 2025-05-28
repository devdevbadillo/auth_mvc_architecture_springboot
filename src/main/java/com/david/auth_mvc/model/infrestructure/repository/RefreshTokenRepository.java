package com.david.auth_mvc.model.infrestructure.repository;

import com.david.auth_mvc.model.domain.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    RefreshToken findRefreshTokenByRefreshTokenId(String refreshTokenId);

    RefreshToken findRefreshTokenByAccessTokenId(Long accessTokenId);

}
