package com.david.auth_mvc.model.business.validators.validation;

import com.david.auth_mvc.model.business.services.interfaces.ITypeTokenService;
import com.david.auth_mvc.model.domain.entity.AccessToken;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.business.services.interfaces.application.ICredentialService;
import com.david.auth_mvc.model.infrestructure.utils.constants.CommonConstants;
import com.david.auth_mvc.model.business.validators.annotation.HasAccessTokenToChangePassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@AllArgsConstructor
@Component
public class HasAccessTokenToChangePasswordValidation  implements ConstraintValidator<HasAccessTokenToChangePassword, String> {
    private final ICredentialService credentialService;
    private final ITypeTokenService typeTokenService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Credential credential = credentialService.getCredentialByEmail(value);
        if (credential == null)  return true;

        Optional<AccessToken> hasAccessToken = credential.getAccessTokens().stream().filter(
                accessToken -> accessToken.getTypeToken().equals( typeTokenService.getTypeToken(CommonConstants.TYPE_ACCESS_TOKEN_TO_CHANGE_PASSWORD) )
        ).findFirst();

        return hasAccessToken.map(accessToken -> accessToken.getExpirationDate().compareTo(new Date()) < 0).orElse(true);
    }
}
