package com.david.auth_mvc.common.exceptions.auth;

public class UserNotVerifiedException extends Exception{

    public UserNotVerifiedException(String message){
        super(message);
    }
}
