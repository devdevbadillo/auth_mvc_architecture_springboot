package com.david.auth_mvc.model.domain.services.application;

public interface IEmailService {
    void sendEmailRecoveryAccount(String email, String token);

    void sendEmailVerifyAccount(String email, String accessToken, String refreshToken);
}
