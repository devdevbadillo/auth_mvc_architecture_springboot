package com.david.auth_mvc.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PairTokenResponse {
    private String accessToken;
    private String refreshToken;
}
