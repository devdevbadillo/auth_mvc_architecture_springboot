package com.david.auth_mvc.model.domain.exceptions.credential;

public class UserNotFoundException extends Exception{

    public UserNotFoundException(String message) {
        super(message);
    }
}
