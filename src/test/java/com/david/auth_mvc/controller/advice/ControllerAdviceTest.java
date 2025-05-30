package com.david.auth_mvc.controller.advice;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.david.auth_mvc.model.domain.exceptions.accessToken.AlreadyHaveAccessTokenToChangePasswordException;
import com.david.auth_mvc.model.domain.exceptions.auth.HasAccessWithOAuth2Exception;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotVerifiedException;
import com.david.auth_mvc.model.domain.exceptions.credential.UserAlreadyExistException;
import jakarta.mail.MessagingException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ControllerAdviceTest {

    @InjectMocks
    private ControllerAdvice controllerAdvice;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;


    @Test
    void handleValidationErrors_WithFieldError_ReturnsFieldErrorMessage() {
        // Arrange
        String expectedErrorMessage = "Email cannot be empty";
        FieldError fieldError = new FieldError("objectName", "email", expectedErrorMessage);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<Map<String, String>> response = controllerAdvice.handleValidationErrors(methodArgumentNotValidException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }

    @Test
    void handleValidationErrors_WithoutFieldError_ReturnsExceptionMessage() {
        // Arrange
        String expectedErrorMessage = "Validation failed";

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        when(methodArgumentNotValidException.getMessage()).thenReturn(expectedErrorMessage);

        // Act
        ResponseEntity<Map<String, String>> response = controllerAdvice.handleValidationErrors(methodArgumentNotValidException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }

    @Test
    void handleMissingRequestHeaderException_ReturnsCorrectResponse() {
        // Arrange
        String expectedErrorMessage = "Required header 'Authorization' is not present";
        MissingRequestHeaderException exception = Mockito.mock(MissingRequestHeaderException.class);
        when(exception.getMessage()).thenReturn(expectedErrorMessage);

        // Act
        ResponseEntity<Map<String, String>> response = controllerAdvice.handleMissingRequestHeaderException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }

    @Test
    void handleConstraintViolationException_ReturnsCorrectResponse() {
        // Arrange
        String expectedErrorMessage = "Validation failed for method parameter";
        ConstraintViolationException exception = Mockito.mock(ConstraintViolationException.class);
        when(exception.getMessage()).thenReturn(expectedErrorMessage);

        // Act
        ResponseEntity<Map<String, String>> response = controllerAdvice.handleConstraintViolationException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }

    @Test
    void handleUserAlreadyExistException_ReturnsCorrectResponse() {
        // Arrange
        String expectedErrorMessage = "User already exists with this email";
        UserAlreadyExistException exception = new UserAlreadyExistException(expectedErrorMessage);

        // Act
        ResponseEntity<Map<String, String>> response = controllerAdvice.handleUserAlreadyExistException(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }

    @Test
    void handleBadCredentialsException_ReturnsCorrectResponse() {
        // Arrange
        String expectedErrorMessage = "Invalid username or password";
        BadCredentialsException exception = new BadCredentialsException(expectedErrorMessage);

        // Act
        ResponseEntity<Map<String, String>> response = controllerAdvice.handleBadCredentialsException(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }

    @Test
    void handleJWTVerificationException_ReturnsCorrectResponse() {
        // Arrange
        String expectedErrorMessage = "Invalid JWT token";
        JWTVerificationException exception = new JWTVerificationException(expectedErrorMessage);

        // Act
        ResponseEntity<Map<String, String>> response = controllerAdvice.handleJWTVerificationException(exception);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }

    @Test
    void handleHaveAccessWithOAuth2Exception_ReturnsCorrectResponse() {
        // Arrange
        String expectedErrorMessage = "User has access with OAuth2";
        HasAccessWithOAuth2Exception exception = new HasAccessWithOAuth2Exception(expectedErrorMessage);

        // Act
        ResponseEntity<Map<String, String>> response = controllerAdvice.handleHaveAccessWithOAuth2Exception(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }

    @Test
    void handleMessagingException_ReturnsCorrectResponse() {
        // Arrange
        String expectedErrorMessage = "Failed to send email";
        MessagingException exception = new MessagingException(expectedErrorMessage);

        // Act
        ResponseEntity<Map<String, String>> response = controllerAdvice.handleMessagingException(exception);

        // Assert
        assertEquals(HttpStatus.FAILED_DEPENDENCY, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }

    @Test
    void handleAlreadyHaveAccessTokenToChangePasswordException_ReturnsCorrectResponse() {
        // Arrange
        String expectedErrorMessage = "Already have access token to change password";
        AlreadyHaveAccessTokenToChangePasswordException exception =
                new AlreadyHaveAccessTokenToChangePasswordException(expectedErrorMessage);

        // Act
        ResponseEntity<Map<String, String>> response =
                controllerAdvice.handleAlreadyHaveAccessTokenToChangePasswordException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }

    @Test
    void handleUserNotVerifiedException_ReturnsCorrectResponse() {
        // Arrange
        String expectedErrorMessage = "User not verified";
        UserNotVerifiedException exception = new UserNotVerifiedException(expectedErrorMessage);

        // Act
        ResponseEntity<Map<String, String>> response = controllerAdvice.handleUserNotVerifiedException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey("message"));
        assertEquals(expectedErrorMessage, response.getBody().get("message"));
    }
}