package com.david.auth_mvc.model.infrestructure.filters.credential;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.david.auth_mvc.model.infrestructure.utils.JwtUtil;
import com.david.auth_mvc.model.infrestructure.utils.constants.CommonConstants;
import com.david.auth_mvc.controller.messages.AuthMessages;
import com.david.auth_mvc.model.infrestructure.utils.constants.routes.CredentialRoutes;
import com.david.auth_mvc.model.domain.entity.RefreshToken;
import com.david.auth_mvc.model.infrestructure.repository.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
@AllArgsConstructor
public class JwtRefreshAccessToVerifyAccount extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwtToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        String path = request.getRequestURI();

        if (jwtToken != null && jwtToken.startsWith("Bearer ") && path.contains(CredentialRoutes.REFRESH_ACCESS_TO_VERIFY_ACCOUNT_URL)) {
            jwtToken = jwtToken.replace("Bearer ", "");

            try {
                DecodedJWT decodedJWT = jwtUtil.validateToken(jwtToken);
                jwtUtil.validateTypeToken(decodedJWT, CommonConstants.TYPE_REFRESH_TOKEN_TO_VERIFY_ACCOUNT);

                String refreshTokenId = jwtUtil.getSpecificClaim(decodedJWT, "jti").asString();
                RefreshToken refreshToken = this.refreshTokenRepository.findRefreshTokenByRefreshTokenId(refreshTokenId);

                if (refreshToken == null) throw new JWTVerificationException(AuthMessages.INVALID_TOKEN_ERROR);
                if(refreshToken.getAccessToken().getExpirationDate().compareTo(new Date()) > 0)  throw new JWTVerificationException(AuthMessages.INVALID_TOKEN_ERROR);

                request.setAttribute("credential", refreshToken.getCredential());
                request.setAttribute("refreshToken", jwtToken);
            } catch (JWTVerificationException ex) {
                this.jwtUtil.handleInvalidToken(response, ex.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
