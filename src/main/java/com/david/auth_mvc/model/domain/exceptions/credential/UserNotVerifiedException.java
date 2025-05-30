package com.david.auth_mvc.model.domain.exceptions.credential;

public class UserNotVerifiedException extends Exception{

    public UserNotVerifiedException(String message){
        super(message);
    }
}
