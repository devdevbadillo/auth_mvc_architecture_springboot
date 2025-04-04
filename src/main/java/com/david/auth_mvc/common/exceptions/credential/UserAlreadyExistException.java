package com.david.auth_mvc.common.exceptions.credential;

public class UserAlreadyExistException extends Exception {
    
    public UserAlreadyExistException(String message) {
        super(message);
    }
}
