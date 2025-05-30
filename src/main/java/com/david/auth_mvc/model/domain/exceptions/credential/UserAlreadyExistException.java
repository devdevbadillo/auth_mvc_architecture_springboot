package com.david.auth_mvc.model.domain.exceptions.credential;

public class UserAlreadyExistException extends Exception {
    
    public UserAlreadyExistException(String message) {
        super(message);
    }
}
