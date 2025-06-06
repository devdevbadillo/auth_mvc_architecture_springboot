package com.david.auth_mvc.model.infrestructure.services.impl;

import com.david.auth_mvc.controller.messages.EmailMessages;
import com.david.auth_mvc.model.infrestructure.services.interfaces.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmailRecoveryAccount(String email, String accessToken) {
        String htmlMsg = """
                        <h1>Change Password Request</h1>
                        <p>We received a request to reset your password for your account associated with this email address.</p>

                        <div class="info">
                            <p><strong>Email:</strong> %s</p>
                        </div>
                        <p>To change your password, please click the link below:</p>
                        <ol>
                            <li><a href="http://localhost:4200/auth/change-password?accessToken=%s">Change Your Password</a></li>
                        </ol>
                        <p>If you didn't request a password reset, you can safely ignore this email.</p>

                        <div class="warning">
                            <p><strong>Important:</strong></p>
                            <ul>
                                <li>For security reasons, this link will expire in 10 minutes.</li>
                                <li>Never share this information with anyone.</li>
                            </ul>
                        </div>
                        <p>If you have any questions or need further assistance, feel free to contact our support team.</p>
                """;

        sendEmail(email, accessToken, htmlMsg);
    }

    @Override
    public void sendEmailVerifyAccount(String email, String accessToken, String refreshToken) {
        String htmlMsg = """
                        <h1>Verify Account</h1>
                        <p>We received a request to verify your account for your account associated with this email address.</p>

                        <div class="info">
                            <p><strong>Email:</strong> %s</p>
                        </div>
                        <p>To verify your account, please click the link below:</p>
                        <ol>
                            <li><a href="http://localhost:4200/auth/verify-account?accessToken=%s">Verify Account</a></li>
                            <li>If the link has expired, click here: <a href="http://localhost:4200/auth/refresh-token-to-verify-account?refreshToken=%s">New access</a></li>
                        </ol>
                        <p>If you didn't request a password reset, you can safely ignore this email.</p>

                        <div class="warning">
                            <p><strong>Important:</strong></p>
                            <ul>
                                <li>For security reasons, this link will expire in 1 hour.</li>
                                <li>Never share this information with anyone.</li>
                            </ul>
                        </div>
                        <p>If you have any questions or need further assistance, feel free to contact our support team.</p>
                """;

        sendEmail(email, accessToken, refreshToken, htmlMsg);
    }


    private void sendEmail(String email, String accessToken, String htmlMsg)  {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Change Password Request");
            helper.setText(String.format(htmlMsg, email, accessToken), true); // true indicates HTML
            helper.setFrom("noreply@apptest.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendException(EmailMessages.ERROR_SENDING_EMAIL);
        }
    }

    private void sendEmail(String email, String accessToken, String refreshToken, String htmlMsg) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("Verify Account");
            helper.setText(String.format(htmlMsg, email, accessToken, refreshToken), true); // true indicates HTML
            helper.setFrom("noreply@apptest.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailSendException(EmailMessages.ERROR_SENDING_EMAIL);
        }
    }
}
