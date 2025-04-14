package com.david.auth_mvc.model.service.interfaces;

import com.david.auth_mvc.model.domain.dto.response.MessageResponse;

public interface IUserService {

    MessageResponse signOut(String accessTokenId);
}
