package com.david.auth_mvc.model.business.validators.annotation;

import com.david.auth_mvc.controller.messages.CredentialMessages;
import com.david.auth_mvc.model.business.validators.validation.HasAccessTokenToChangePasswordValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HasAccessTokenToChangePasswordValidation.class)
public @interface HasAccessTokenToChangePassword {
    String message() default CredentialMessages.ALREADY_HAVE_ACCESS_TOKEN_TO_CHANGE_PASSWORD;

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
