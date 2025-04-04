package com.david.auth_mvc.controller.security;

import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.common.utils.constants.routes.AuthRoutes;
import com.david.auth_mvc.common.utils.constants.routes.CredentialRoutes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.david.auth_mvc.common.utils.JwtUtil;
import com.david.auth_mvc.controller.security.filters.JwtValidateFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(http -> {
                    http.requestMatchers(
                                    HttpMethod.POST,
                                    CommonConstants.PUBLIC_URL + AuthRoutes.SIGNIN_URL,
                                    CommonConstants.PUBLIC_URL + CredentialRoutes.SIGNUP_URL,
                                    CommonConstants.PUBLIC_URL + AuthRoutes.REFRESH_TOKEN_URL
                            )
                            .permitAll();
                    http.requestMatchers(HttpMethod.GET, CommonConstants.SECURE_URL + "/**").hasRole("USER");

                    http.anyRequest().permitAll();
                })
                .addFilterBefore(new JwtValidateFilter(jwtUtil), BasicAuthenticationFilter.class);;
        return httpSecurity.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
