package com.david.auth_mvc.controller.rest;

import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.routes.AuthRoutes;
import com.david.auth_mvc.model.domain.dto.request.SignInRequest;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.TypeToken;
import com.david.auth_mvc.model.repository.CredentialRepository;
import com.david.auth_mvc.model.repository.TypeTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TypeTokenRepository typeTokenRepository;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    public void setup() {
        Credential credential = new Credential();
        credential.setEmail(TEST_EMAIL);
        credential.setName("Test User");
        credential.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        credential.setIsVerified(true);
        credential.setIsAccesOauth(false);

        credentialRepository.save(credential);

        Credential oauth2User = new Credential();
        oauth2User.setEmail("oauth2@example.com");
        oauth2User.setName("OAuth2 User");
        oauth2User.setIsVerified(true);
        oauth2User.setIsAccesOauth(true);

        credentialRepository.save(oauth2User);

        Credential unverifiedUser = new Credential();
        unverifiedUser.setEmail("unverified@example.com");
        unverifiedUser.setName("Unverified User");
        unverifiedUser.setPassword(passwordEncoder.encode("password"));
        unverifiedUser.setIsVerified(false);
        unverifiedUser.setIsAccesOauth(false);

        credentialRepository.save(unverifiedUser);

        TypeToken typeAccessToken = new TypeToken();
        typeAccessToken.setType(CommonConstants.TYPE_ACCESS_TOKEN);
        typeTokenRepository.save(typeAccessToken);

        TypeToken typeRefreshToken = new TypeToken();
        typeRefreshToken.setType(CommonConstants.TYPE_REFRESH_TOKEN);
        typeTokenRepository.save(typeRefreshToken);
    }

    @AfterEach
    public void tearDown() {
        credentialRepository.deleteAll();
        typeTokenRepository.deleteAll();
    }

    @Test
    public void signIn_withValidCredentials_returnsToken() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        // Act & Assert
        mockMvc.perform(post(CommonConstants.PUBLIC_URL + AuthRoutes.SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    public void signIn_withInvalidCredentials_returnsBadCredentials() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword("wrong-password");

        // Act & Assert
        mockMvc.perform(post(CommonConstants.PUBLIC_URL + AuthRoutes.SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void signIn_withOAuth2User_returnsError() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setEmail("oauth2@example.com");
        request.setPassword("password");

        // Act & Assert
        mockMvc.perform(post(CommonConstants.PUBLIC_URL + AuthRoutes.SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void signIn_withUnverifiedUser_returnsError() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setEmail("unverified@example.com");
        request.setPassword("password");

        // Act & Assert
        mockMvc.perform(post(CommonConstants.PUBLIC_URL + AuthRoutes.SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void refreshToken_withValidToken_returnsNewToken() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        MvcResult signInResult = mockMvc.perform(post(CommonConstants.PUBLIC_URL + AuthRoutes.SIGNIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = signInResult.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(response).get("refreshToken").asText();

        // Act & Assert
        mockMvc.perform(post(CommonConstants.PUBLIC_URL + AuthRoutes.REFRESH_TOKEN_URL)
                        .header("refreshToken", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    public void refreshToken_withInvalidToken_returnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post(CommonConstants.PUBLIC_URL + AuthRoutes.REFRESH_TOKEN_URL)
                        .header("refreshToken", "invalid-refresh-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void refreshToken_withMissingToken_returnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post(CommonConstants.PUBLIC_URL + AuthRoutes.REFRESH_TOKEN_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void authenticationOAuth2Error_returnsMessage() throws Exception {
        // Act & Assert
        mockMvc.perform(get(CommonConstants.PUBLIC_URL + AuthRoutes.OAUTH2_ERROR_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Authentication error"));
    }
}