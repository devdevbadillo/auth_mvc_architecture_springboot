package com.david.auth_mvc.model.repository;

import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.TypeToken;
import com.david.auth_mvc.model.infrestructure.repository.AccessTokenRepository;
import com.david.auth_mvc.model.infrestructure.repository.CredentialRepository;
import com.david.auth_mvc.model.infrestructure.repository.TypeTokenRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
public class AccessTokenRepositoryTest {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private TypeTokenRepository typeTokenRepository;

    private static Credential credential;
    private static TypeToken typeToken;

    @BeforeEach
    void setUp() {
         credential = credentialRepository.save(generateCredentialTest());
         typeToken = typeTokenRepository.save(generateTypeTokenTest(CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP));
    }

    @AfterEach
    void tearDown() {
        credentialRepository.delete(credential);
        typeTokenRepository.delete(typeToken);
    }

    @Test
    void test_save_access_token_success() {
        // given
        AccessToken accessTokenRequest = generateAccessTokenTest(credential, typeToken);

        // when
        AccessToken accessTokenResponse = accessTokenRepository.save(accessTokenRequest);

        // then
        assertNotNull(accessTokenResponse);
    }

    @Test
    void get_access_token_by_credential_and_type_token_success() {
        // given
        AccessToken accessTokenRequest = generateAccessTokenTest(credential, typeToken);
        accessTokenRepository.save(accessTokenRequest);

        // when
        AccessToken accessTokenResponse = accessTokenRepository.getTokenByCredentialAndTypeToken(credential, typeToken);

        // then
        assertNotNull(accessTokenResponse);
    }


    @Test
    void get_access_token_by_access_token_id_success() {
        // given
        AccessToken accessTokenRequest = generateAccessTokenTest(credential, typeToken);
        accessTokenRepository.save(accessTokenRequest);

        // when
        AccessToken accessTokenResponse = accessTokenRepository.getTokenByAccessTokenId(accessTokenRequest.getAccessTokenId());

        // then
        assertNotNull(accessTokenResponse);
    }


    @Test
    void get_access_token_by_access_token_id_not_found() {
        // given
        String accessTokenId = "1";

        // when
        AccessToken accessTokenResponse = accessTokenRepository.getTokenByAccessTokenId(accessTokenId);

        // then
        assertNull(accessTokenResponse);
    }


    @Test
    void get_access_token_by_credential_and_type_token_not_found() {
        // given
        TypeToken typeToken = typeTokenRepository.save(generateTypeTokenTest(CommonConstants.TYPE_ACCESS_TOKEN_TO_CHANGE_PASSWORD));

        // when
        AccessToken accessTokenResponse = accessTokenRepository.getTokenByCredentialAndTypeToken(credential, typeToken);

        // then
        assertNull(accessTokenResponse);
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

    private Credential generateCredentialTest(){
        return Credential.builder()
                .email("test@email.com")
                .name("Test")
                .password("123456789")
                .isVerified(false)
                .isAccesOauth(false)
                .build();
    }

    private TypeToken generateTypeTokenTest(String typeToken) {
        return TypeToken.builder()
                .type(typeToken)
                .build();
    }
}
