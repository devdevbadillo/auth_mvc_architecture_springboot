package com.david.auth_mvc.controller.rest;

import com.david.auth_mvc.common.mapper.AccessTokenEntityMapper;
import com.david.auth_mvc.common.mapper.CredentialEntityMapper;
import com.david.auth_mvc.common.mapper.RefreshTokenEntityMapper;
import com.david.auth_mvc.common.utils.JwtUtil;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.routes.UserRoutes;
import com.david.auth_mvc.model.domain.dto.request.SignUpRequest;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.entity.RefreshToken;
import com.david.auth_mvc.model.domain.entity.TypeToken;
import com.david.auth_mvc.model.repository.AccessTokenRepository;
import com.david.auth_mvc.model.repository.CredentialRepository;
import com.david.auth_mvc.model.repository.RefreshTokenRepository;
import com.david.auth_mvc.model.repository.TypeTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
public class UserControllerTest {

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

    // mappers
    @Autowired
    private AccessTokenEntityMapper accessTokenEntityMapper;

    @Autowired
    private RefreshTokenEntityMapper refreshTokenEntityMapper;

    @Autowired
    private CredentialEntityMapper credentialEntityMapper;
    // ----------------------------------------------

    // utils
    @Autowired
    private JwtUtil jwtUtil;
    // ----------------------------------------------

    String verificationToken;
    AccessToken accessToken;
    @BeforeEach
    public void setup() {
        Credential testUser = credentialEntityMapper.toCredentialEntity(new SignUpRequest("Test User", "test@example.com", "Password123!"));
        testUser.setIsVerified(true);
        testUser = credentialRepository.save(testUser);

        TypeToken typeAccessToken = new TypeToken();
        typeAccessToken.setType(CommonConstants.TYPE_ACCESS_TOKEN);
        typeTokenRepository.save(typeAccessToken);

        TypeToken typeRefreshToken = new TypeToken();
        typeRefreshToken.setType(CommonConstants.TYPE_REFRESH_TOKEN);
        typeTokenRepository.save(typeRefreshToken);

        verificationToken = jwtUtil.generateAccessToken(testUser, CommonConstants.EXPIRATION_TOKEN_TO_ACCESS_APP, CommonConstants.TYPE_ACCESS_TOKEN);
        accessToken = accessTokenEntityMapper.toTokenEntity(verificationToken, testUser, CommonConstants.TYPE_ACCESS_TOKEN);
        accessTokenRepository.save(accessToken);

        String refreshTokenVerification = jwtUtil.generateRefreshToken(testUser, CommonConstants.EXPIRATION_REFRESH_TOKEN_TO_ACCESS_APP, CommonConstants.TYPE_REFRESH_TOKEN);
        RefreshToken refreshToken = refreshTokenEntityMapper.toRefreshTokenEntity(refreshTokenVerification, testUser, CommonConstants.TYPE_REFRESH_TOKEN, accessToken);
        refreshTokenRepository.save(refreshToken);
    }

    @AfterEach
    public void tearDown() {
        // Clean up database
        credentialRepository.deleteAll();
        typeTokenRepository.deleteAll();
        accessTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
    }

    @Test
    void testHome_Success() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get(CommonConstants.SECURE_URL + UserRoutes.USER)
                        .header("Authorization", "Bearer " + verificationToken)
                        .requestAttr("accessTokenId", accessToken.getAccessTokenId()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void testHome_Unauthorized() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get(CommonConstants.SECURE_URL + UserRoutes.USER)
                        .header("Authorization", "Bearer " + "invalid-token"))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void testSignOut_Success() throws Exception {
        // When
        MvcResult result = mockMvc.perform(post(CommonConstants.SECURE_URL + UserRoutes.SIGN_OUT_URL)
                        .header("Authorization", "Bearer " + verificationToken)
                        .requestAttr("accessTokenId", accessToken.getAccessTokenId()))
                .andExpect(status().isOk())
                .andReturn();
    }
}
