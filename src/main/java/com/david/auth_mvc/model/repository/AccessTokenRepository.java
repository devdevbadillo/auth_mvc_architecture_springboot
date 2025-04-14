package com.david.auth_mvc.model.repository;

import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.TypeToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTokenRepository extends CrudRepository<AccessToken, Long> {
    AccessToken getTokenByAccessTokenId(String accessTokenId);

    AccessToken getTokenByCredentialAndTypeToken(Credential credential, TypeToken typeToken);
}
