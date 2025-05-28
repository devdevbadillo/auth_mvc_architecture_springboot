package com.david.auth_mvc.model.repository;

import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.RefreshToken;
import com.david.auth_mvc.model.domain.entity.TypeToken;
import com.david.auth_mvc.model.infrestructure.repository.AccessTokenRepository;
import com.david.auth_mvc.model.infrestructure.repository.CredentialRepository;
import com.david.auth_mvc.model.infrestructure.repository.RefreshTokenRepository;
import com.david.auth_mvc.model.infrestructure.repository.TypeTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
public class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private TypeTokenRepository typeTokenRepository;

    private static Credential credential;
    private static TypeToken typeToken;
    private static TypeToken typeAccessToken;
    private static AccessToken accessToken;

    @BeforeEach
    void setUp() {
        credential  = credentialRepository.save(generateCredentialTest());
        typeAccessToken   = typeTokenRepository.save(generateTypeTokenTest(CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP));
        accessToken = accessTokenRepository.save(generateAccessTokenTest(credential, typeAccessToken));

        typeToken = typeTokenRepository.save(generateTypeTokenTest(CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP));
    }

    @AfterEach
    void tearDown() {
        credentialRepository.delete(credential);
        typeTokenRepository.delete(typeAccessToken);
        accessTokenRepository.delete(accessToken);

        typeTokenRepository.delete(typeToken);
    }

    @Test
    void test_save_refresh_token_success() {
        // given
        RefreshToken refreshToken = generateRefreshTokenTest(accessToken, credential, typeToken);

        // when
        RefreshToken refreshTokenResponse = refreshTokenRepository.save(refreshToken);

        // then
        assertNotNull(refreshTokenResponse);
    }

    @Test
    void test_find_refresh_token_by_refresh_token_id_success() {
        // given
        RefreshToken refreshToken = generateRefreshTokenTest(accessToken, credential, typeToken);
        refreshTokenRepository.save(refreshToken);

        // when
        RefreshToken refreshTokenResponse = refreshTokenRepository.findRefreshTokenByRefreshTokenId(refreshToken.getRefreshTokenId());

        // then
        assertNotNull(refreshTokenResponse);
    }

    @Test
    void test_find_refresh_token_by_access_token_id_success() {
        // given
        RefreshToken refreshToken = generateRefreshTokenTest(accessToken, credential, typeToken);
        refreshTokenRepository.save(refreshToken);

        // when
        RefreshToken refreshTokenResponse = refreshTokenRepository.findRefreshTokenByAccessTokenId(refreshToken.getAccessToken().getId());

        // then
        assertNotNull(refreshTokenResponse);
    }

    @Test
    void test_find_refresh_token_by_refresh_token_id_not_found() {
        // given
        String refreshTokenId = "1";
        // when
        RefreshToken refreshTokenResponse = refreshTokenRepository.findRefreshTokenByRefreshTokenId(refreshTokenId);

        // then
        assertNull(refreshTokenResponse);
    }

    @Test
    void test_find_refresh_token_by_access_token_id_not_found() {
        // given
        Long accessTokenId = 1L;
        // when
        RefreshToken refreshTokenResponse = refreshTokenRepository.findRefreshTokenByAccessTokenId(accessTokenId);

        // then
        assertNull(refreshTokenResponse);
    }

    private RefreshToken generateRefreshTokenTest(AccessToken accessToken, Credential credential, TypeToken typeToken) {
        return RefreshToken.builder()
                .accessToken(accessToken)
                .credential(credential)
                .typeToken(typeToken)
                .refreshTokenId("d341-3562-487f-a9c0-a29f2012a210")
                .creationDate(new Date())
                .expirationDate(new Date())
                .build();
    }

    private Credential generateCredentialTest() {
        return Credential.builder()
                .email("test@test.com")
                .password("123456")
                .name("Test")
                .isVerified(false)
                .isAccesOauth(false)
                .build();
    }

    private TypeToken generateTypeTokenTest(String typeToken) {
        return TypeToken.builder()
                .type(typeToken)
                .build();
    }

    private AccessToken generateAccessTokenTest(Credential credential, TypeToken typeToken) {
        return AccessToken.builder()
                .credential(credential)
                .typeToken(typeToken)
                .creationDate(new Date())
                .expirationDate(new Date())
                .accessTokenId("0542dabe-3562-487f-a9c0-a29f2012a210")
                .build();
    }

}
