package com.david.auth_mvc.controller.dto.request;

import com.david.auth_mvc.model.business.validators.annotation.HasAccessTokenToChangePassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RecoveryAccountRequest {
    @NotBlank
    @Email
    @HasAccessTokenToChangePassword
    private String email;
}
