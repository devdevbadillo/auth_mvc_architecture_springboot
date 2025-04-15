package com.david.auth_mvc.controller.security.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.david.auth_mvc.common.utils.JwtUtil;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.messages.AuthMessages;
import com.david.auth_mvc.common.utils.constants.routes.CredentialRoutes;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.repository.AccessTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtChangePasswordFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AccessTokenRepository accessTokenRepository;


    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        String path = request.getRequestURI();

        if (jwtToken != null && jwtToken.startsWith("Bearer ") && path.contains(CredentialRoutes.CHANGE_PASSWORD_URL)) {
            jwtToken = jwtToken.replace("Bearer ", "");

            try {
                DecodedJWT decodedJWT = jwtUtil.validateToken(jwtToken);
                jwtUtil.validateTypeToken(decodedJWT, CommonConstants.TYPE_CHANGE_PASSWORD);

                String username = jwtUtil.extractUser(decodedJWT);
                String accessTokenId = jwtUtil.getSpecificClaim(decodedJWT, "jti").asString();

                AccessToken accessToken = this.accessTokenRepository.getTokenByAccessTokenId(accessTokenId);

                if( accessToken == null ) throw new JWTVerificationException(AuthMessages.INVALID_TOKEN_ERROR);

                request.setAttribute("accessTokenId", accessTokenId);
                request.setAttribute("email", username);
            } catch (JWTVerificationException ex) {
                this.jwtUtil.handleInvalidToken(response, ex.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
