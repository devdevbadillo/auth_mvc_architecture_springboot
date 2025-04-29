package com.david.auth_mvc.model.service;

import com.david.auth_mvc.common.utils.constants.EmailMessages;
import com.david.auth_mvc.model.service.implementation.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

        @Mock
        private JavaMailSender mailSender;

        @Mock
        private MimeMessage mimeMessage;

        @InjectMocks
        private EmailServiceImpl emailService;

        @Captor
        private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

        private final String testEmail = "test@example.com";
        private final String testAccessToken = "test-access-token-123";
        private final String testRefreshToken = "test-refresh-token-456";

        @BeforeEach
        void setUp() {
                when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        }

        @Test
        void sendEmailRecoveryAccount_ShouldSendEmail() throws MessagingException {
                // Arrange
                doNothing().when(mailSender).send(any(MimeMessage.class));

                // Act
                assertDoesNotThrow(() -> emailService.sendEmailRecoveryAccount(testEmail, testAccessToken));

                // Assert
                verify(mailSender, times(1)).createMimeMessage();
                verify(mailSender, times(1)).send(mimeMessageCaptor.capture());
        }

        @Test
        void sendEmailRecoveryAccount_ShouldThrowException_WhenMailSenderFails() throws MessagingException {
                // Arrange
                doThrow(new MailSendException(EmailMessages.ERROR_SENDING_EMAIL))
                        .when(mailSender).send(any(MimeMessage.class));

                // Act
                MailSendException exception = assertThrows(MailSendException.class, () ->
                        emailService.sendEmailVerifyAccount(testEmail, testAccessToken, testRefreshToken)
                );

                // Assert
                assertEquals(EmailMessages.ERROR_SENDING_EMAIL, exception.getMessage());
                verify(mailSender, times(1)).createMimeMessage();
        }

        @Test
        void sendEmailVerifyAccount_ShouldSendEmail() throws MessagingException {
                // Arrange
                doNothing().when(mailSender).send(any(MimeMessage.class));

                // Act
                assertDoesNotThrow(() -> emailService.sendEmailVerifyAccount(testEmail, testAccessToken, testRefreshToken));

                // Assert
                verify(mailSender, times(1)).createMimeMessage();
                verify(mailSender, times(1)).send(mimeMessageCaptor.capture());
        }

        @Test
        void sendEmailVerifyAccount_ShouldThrowException_WhenMailSenderFails() throws MessagingException {
                // Arrange
                doThrow(new MailSendException(EmailMessages.ERROR_SENDING_EMAIL)).when(mailSender).send(any(MimeMessage.class));

                // Act
                MailSendException exception = assertThrows(
                        MailSendException.class,
                        () -> emailService.sendEmailVerifyAccount(testEmail, testAccessToken, testRefreshToken)
                );

                // Assert
                assertEquals(EmailMessages.ERROR_SENDING_EMAIL, exception.getMessage());
                verify(mailSender, times(1)).createMimeMessage();
        }
}