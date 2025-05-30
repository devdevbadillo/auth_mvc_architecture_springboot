package com.david.auth_mvc.model.business.services.interfaces.application;

import com.david.auth_mvc.controller.dto.response.MessageResponse;

public interface IUserService {

    MessageResponse signOut(String accessTokenId);
}
