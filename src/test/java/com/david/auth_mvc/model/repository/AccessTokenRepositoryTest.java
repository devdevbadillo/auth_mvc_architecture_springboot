package com.david.auth_mvc.model.repository;

import com.david.auth_mvc.model.domain.entity.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

@ActiveProfiles("test")
@DataJpaTest
public class AccessTokenRepositoryTest {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private TypeTokenRepository typeTokenRepository;


    private AccessToken generateAccessTokenTest() {
        return AccessToken.builder()
                .creationDate(new Date())
                .expirationDate(new Date())
                .accessTokenId("0542dabe-3562-487f-a9c0-a29f2012a210")
                .build();
    }
}
