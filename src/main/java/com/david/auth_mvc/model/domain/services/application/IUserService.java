package com.david.auth_mvc.model.domain.services.application;

import com.david.auth_mvc.model.domain.dto.response.MessageResponse;

public interface IUserService {

    MessageResponse signOut(String accessTokenId);
}
