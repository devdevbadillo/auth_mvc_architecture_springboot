package com.david.auth_mvc.controller.rest;

import com.david.auth_mvc.model.infrestructure.utils.constants.CommonConstants;
import com.david.auth_mvc.model.infrestructure.utils.constants.routes.UserRoutes;
import com.david.auth_mvc.model.business.services.interfaces.application.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.david.auth_mvc.controller.dto.response.MessageResponse;

@AllArgsConstructor
@RestController
@RequestMapping(path = CommonConstants.SECURE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    private final IUserService userService;

    @GetMapping(UserRoutes.USER)
    public ResponseEntity<MessageResponse> home() {
        return ResponseEntity.ok(new MessageResponse("welcome!"));
    }

    @PostMapping(UserRoutes.SIGN_OUT_URL)
    public ResponseEntity<MessageResponse> signOut(HttpServletRequest request) {
        String accessTokenId =(String) request.getAttribute("accessTokenId");

        return ResponseEntity.ok(this.userService.signOut(accessTokenId));
    }
}
