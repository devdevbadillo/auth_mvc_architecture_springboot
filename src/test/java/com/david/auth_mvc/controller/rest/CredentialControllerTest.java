package com.david.auth_mvc.controller.rest;

import com.david.auth_mvc.common.mapper.AccessTokenEntityMapper;
import com.david.auth_mvc.common.mapper.CredentialEntityMapper;
import com.david.auth_mvc.common.mapper.RefreshTokenEntityMapper;
import com.david.auth_mvc.common.utils.JwtUtil;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.routes.CredentialRoutes;
import com.david.auth_mvc.model.domain.dto.request.ChangePasswordRequest;
import com.david.auth_mvc.model.domain.dto.request.RecoveryAccountRequest;
import com.david.auth_mvc.model.domain.dto.request.SignUpRequest;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.RefreshToken;
import com.david.auth_mvc.model.domain.entity.TypeToken;
import com.david.auth_mvc.model.infrestructure.repository.AccessTokenRepository;
import com.david.auth_mvc.model.infrestructure.repository.CredentialRepository;
import com.david.auth_mvc.model.infrestructure.repository.RefreshTokenRepository;
import com.david.auth_mvc.model.infrestructure.repository.TypeTokenRepository;
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


import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CredentialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // repositories
    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private TypeTokenRepository typeTokenRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    // ----------------------------------------------

    // utils
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccessTokenEntityMapper accessTokenEntityMapper;

    @Autowired
    private RefreshTokenEntityMapper refreshTokenEntityMapper;

    @Autowired
    private CredentialEntityMapper credentialEntityMapper;
    // ----------------------------------------------

    private final String BASE_URL = CommonConstants.PUBLIC_URL;
    private Credential testUser;


    @BeforeEach
    public void setup() {
        testUser = credentialEntityMapper.toCredentialEntity(new SignUpRequest("Test User", "test@example.com", "Password123!"));
        testUser = credentialRepository.save(testUser);

        TypeToken typeAccessToken = new TypeToken();
        typeAccessToken.setType(CommonConstants.TYPE_ACCESS_TOKEN_TO_ACCESS_APP);
        typeTokenRepository.save(typeAccessToken);

        TypeToken typeRefreshToken = new TypeToken();
        typeRefreshToken.setType(CommonConstants.TYPE_REFRESH_TOKEN_TO_ACCESS_APP);
        typeTokenRepository.save(typeRefreshToken);

        TypeToken typeRecoveryToken = new TypeToken();
        typeRecoveryToken.setType(CommonConstants.TYPE_ACCESS_TOKEN_TO_CHANGE_PASSWORD);
        typeTokenRepository.save(typeRecoveryToken);

        TypeToken typeRecoveryTokenToVerifyAccount = new TypeToken();
        typeRecoveryTokenToVerifyAccount.setType(CommonConstants.TYPE_REFRESH_TOKEN_TO_VERIFY_ACCOUNT);
        typeTokenRepository.save(typeRecoveryTokenToVerifyAccount);

        TypeToken typeTokenVerifyAccount = new TypeToken();
        typeTokenVerifyAccount.setType(CommonConstants.TYPE_ACCESS_TOKEN_TO_VERIFY_ACCOUNT);
        typeTokenRepository.save(typeTokenVerifyAccount);
    }

    @AfterEach
    public  void tearDown() {
        // Clean up database
        credentialRepository.deleteAll();
        typeTokenRepository.deleteAll();
    }


    @Test
    public void testSignUp_Success() throws Exception {
        // Given
        SignUpRequest signUpRequest = new SignUpRequest("New User","new-user@example.com", "Password123!");

        // When & Then
        mockMvc.perform(post(BASE_URL + CredentialRoutes.SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void testSignUp_UserAlreadyExists() throws Exception {
        // Given
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setEmail(testUser.getEmail()); // Use existing email
        signUpRequest.setPassword("Password123!");
        signUpRequest.setName("New User");

        // When & Then.
        mockMvc.perform(post(BASE_URL + CredentialRoutes.SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testVerifyAccount_Success() throws Exception {
        // Given
        String verificationToken = jwtUtil.generateAccessToken(testUser, CommonConstants.EXPIRATION_TOKEN_TO_VERIFY_ACCOUNT, CommonConstants.TYPE_ACCESS_TOKEN_TO_VERIFY_ACCOUNT);
        AccessToken accessToken = accessTokenEntityMapper.toTokenEntity(verificationToken, testUser, CommonConstants.TYPE_ACCESS_TOKEN_TO_VERIFY_ACCOUNT);
        accessTokenRepository.save(accessToken);

        String refreshTokenVerification = jwtUtil.generateRefreshToken(testUser, CommonConstants.EXPIRATION_REFRESH_TOKEN_TO_VERIFY_ACCOUNT, CommonConstants.TYPE_REFRESH_TOKEN_TO_VERIFY_ACCOUNT);
        RefreshToken refreshToken = refreshTokenEntityMapper.toRefreshTokenEntity(refreshTokenVerification, testUser, CommonConstants.TYPE_REFRESH_TOKEN_TO_VERIFY_ACCOUNT, accessToken);
        refreshTokenRepository.save(refreshToken);


        // When
        MvcResult result = mockMvc.perform(patch(BASE_URL + CredentialRoutes.VERIFY_ACCOUNT_URL)
                        .header("Authorization", "Bearer " + verificationToken)
                        .requestAttr("accessTokenId", accessToken.getAccessTokenId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        // Then
        Credential updatedUser = credentialRepository.getCredentialByEmail(testUser.getEmail());
        assertTrue(updatedUser.getIsVerified());
    }

    @Test
    public void testRecoveryAccount_Success() throws Exception {
        // Given
        testUser.setIsVerified(true);
        credentialRepository.save(testUser);
        RecoveryAccountRequest recoveryRequest = new RecoveryAccountRequest();
        recoveryRequest.setEmail(testUser.getEmail());

        // When & Then
        mockMvc.perform(post(BASE_URL + CredentialRoutes.RECOVERY_ACCOUNT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recoveryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void testRecoveryAccount_UserNotFound() throws Exception {
        // Given
        RecoveryAccountRequest recoveryRequest = new RecoveryAccountRequest();
        recoveryRequest.setEmail("non-existent@example.com");

        // When & Then
        mockMvc.perform(post(BASE_URL + CredentialRoutes.RECOVERY_ACCOUNT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recoveryRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testViewChangePassword_Success() throws Exception {
        // Given
        String tokenToChangePassword = jwtUtil.generateAccessToken(testUser, CommonConstants.EXPIRATION_TOKEN_TO_CHANGE_PASSWORD, CommonConstants.TYPE_ACCESS_TOKEN_TO_CHANGE_PASSWORD);
        AccessToken accessToken = accessTokenEntityMapper.toTokenEntity(tokenToChangePassword, testUser, CommonConstants.TYPE_ACCESS_TOKEN_TO_CHANGE_PASSWORD);
        accessTokenRepository.save(accessToken);

        // When & Then
        mockMvc.perform(get(BASE_URL + CredentialRoutes.CHANGE_PASSWORD_URL)
                        .header("Authorization", "Bearer " + tokenToChangePassword)
                        .requestAttr("accessTokenId", accessToken.getAccessTokenId())
                        .requestAttr("email", testUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ok"));
    }

    @Test
    public void testChangePassword_Success() throws Exception {
        // Given
        String tokenToChangePassword = jwtUtil.generateAccessToken(testUser, CommonConstants.EXPIRATION_TOKEN_TO_CHANGE_PASSWORD, CommonConstants.TYPE_ACCESS_TOKEN_TO_CHANGE_PASSWORD);
        AccessToken accessToken = accessTokenEntityMapper.toTokenEntity(tokenToChangePassword, testUser, CommonConstants.TYPE_ACCESS_TOKEN_TO_CHANGE_PASSWORD);
        accessTokenRepository.save(accessToken);

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setPassword("NewPassword123!");
        changePasswordRequest.setRepeatPassword("NewPassword123!");

        // When & Then
        mockMvc.perform(patch(BASE_URL + CredentialRoutes.CHANGE_PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest))
                        .header("Authorization", "Bearer " + tokenToChangePassword)
                        .requestAttr("email", testUser.getEmail())
                        .requestAttr("accessTokenId", accessToken.getAccessTokenId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    public void testRefreshAccessToVerifyAccount_Success() throws Exception {
        // Given
        String verificationToken = jwtUtil.generateAccessToken(testUser, CommonConstants.EXPIRATION_TOKEN_TO_VERIFY_ACCOUNT, CommonConstants.TYPE_ACCESS_TOKEN_TO_VERIFY_ACCOUNT);
        AccessToken accessToken = accessTokenEntityMapper.toTokenEntity(verificationToken, testUser, CommonConstants.TYPE_ACCESS_TOKEN_TO_VERIFY_ACCOUNT);
        accessToken.setExpirationDate(new Date());
        accessTokenRepository.save(accessToken);

        String refreshTokenToAccessApp = jwtUtil.generateRefreshToken(testUser, CommonConstants.EXPIRATION_REFRESH_TOKEN_TO_VERIFY_ACCOUNT, CommonConstants.TYPE_REFRESH_TOKEN_TO_VERIFY_ACCOUNT);
        RefreshToken refreshToken = refreshTokenEntityMapper.toRefreshTokenEntity(refreshTokenToAccessApp, testUser, CommonConstants.TYPE_REFRESH_TOKEN_TO_VERIFY_ACCOUNT, accessToken);
        refreshTokenRepository.save(refreshToken);

        // When & Then
        mockMvc.perform(patch(BASE_URL + CredentialRoutes.REFRESH_ACCESS_TO_VERIFY_ACCOUNT_URL)
                        .header("Authorization", "Bearer " + refreshTokenToAccessApp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}