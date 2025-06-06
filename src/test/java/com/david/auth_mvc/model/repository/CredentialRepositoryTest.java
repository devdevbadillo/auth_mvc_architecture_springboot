package com.david.auth_mvc.model.repository;

import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.infrestructure.repository.CredentialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
public class CredentialRepositoryTest {

    @Autowired
    private CredentialRepository credentialRepository;

    @Test
    void test_save_credential_success() {
        // given
        Credential credentialRequest = generateCredentialTest(false);

        // when
        Credential credentialResponse = credentialRepository.save(credentialRequest);

        // then
        assertNotNull(credentialResponse);
    }

    @Test
    void test_save_credential_conflict(){
        // given
        Credential firstCredential = generateCredentialTest(false);
        credentialRepository.save(firstCredential);

        // when
        Credential secondCredential = generateCredentialTest(true);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            credentialRepository.save(secondCredential);
        });
    }

    @Test
    void test_get_credential_by_email_success(){
        // given
        Credential credentialRequest = generateCredentialTest(false);
        credentialRepository.save(credentialRequest);

        // when
        Credential credentialResponse = credentialRepository.getCredentialByEmail(credentialRequest.getEmail());

        // then
        assertNotNull(credentialResponse);
    }

    @Test
    void test_get_credential_by_email_not_found(){
        // given
        Credential credentialRequest = generateCredentialTest(false);

        // when
        Credential credentialResponse = credentialRepository.getCredentialByEmail(credentialRequest.getEmail());

        // then
        assertNull(credentialResponse);
    }

    public Credential generateCredentialTest(Boolean isAccessByOAuth2){
        return Credential.builder()
                .email("test@email.com")
                .name("Test")
                .password("123456789")
                .isVerified(false)
                .isAccesOauth(isAccessByOAuth2)
                .build();
    }
}
