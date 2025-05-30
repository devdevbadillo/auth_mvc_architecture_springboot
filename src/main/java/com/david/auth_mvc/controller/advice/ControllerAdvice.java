package com.david.auth_mvc.controller.advice;

import java.util.Map;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.david.auth_mvc.model.domain.exceptions.auth.HasAccessWithOAuth2Exception;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotVerifiedException;
import com.david.auth_mvc.model.domain.exceptions.credential.UserAlreadyExistException;
import com.david.auth_mvc.model.domain.exceptions.credential.UserNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {
    private static final String KEY_MESSAGE = "message";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {

        String error = ex.getBindingResult()
                .getFieldErrors()
                .stream().map(FieldError::getDefaultMessage)
                .findFirst().orElse(ex.getMessage());

        return new ResponseEntity<>(Map.of(KEY_MESSAGE, error), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingRequestHeaderException(
            MissingRequestHeaderException ex
    ){
        return new ResponseEntity<>(Map.of(KEY_MESSAGE, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(
            ConstraintViolationException ex
    ){
        return new ResponseEntity<>(Map.of(KEY_MESSAGE, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistException(
            UserAlreadyExistException ex
    ){
        return new ResponseEntity<>(Map.of(KEY_MESSAGE, ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(
            BadCredentialsException ex
    ){
        return new ResponseEntity<>(Map.of(KEY_MESSAGE, ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<Map<String, String>> handleJWTVerificationException(JWTVerificationException ex){
        return new ResponseEntity<>(Map.of(KEY_MESSAGE, ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HasAccessWithOAuth2Exception.class)
    public ResponseEntity<Map<String, String>> handleHaveAccessWithOAuth2Exception(HasAccessWithOAuth2Exception ex){
        return new ResponseEntity<>(Map.of(KEY_MESSAGE, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler( MessagingException.class )
    public ResponseEntity<Map<String, String>> handleMessagingException(MessagingException ex){
        return new ResponseEntity<>(Map.of(KEY_MESSAGE, ex.getMessage()), HttpStatus.FAILED_DEPENDENCY);
    }

    @ExceptionHandler( UserNotVerifiedException.class)
    public ResponseEntity<Map<String, String>> handleUserNotVerifiedException(UserNotVerifiedException ex){
        return new ResponseEntity<>(Map.of(KEY_MESSAGE, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex){
        return new ResponseEntity<>(Map.of(KEY_MESSAGE, ex.getMessage()), HttpStatus.NOT_FOUND);
    }
}
