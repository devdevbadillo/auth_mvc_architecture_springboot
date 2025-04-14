package com.david.auth_mvc.common.exceptions.accessToken;

public class AlreadyHaveAccessTokenToChangePasswordException extends Exception {

    public AlreadyHaveAccessTokenToChangePasswordException(String message) {
        super(message);
    }
}
