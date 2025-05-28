package com.david.auth_mvc.model.infrestructure.events;

import com.david.auth_mvc.model.service.interfaces.ITokenService;
import com.david.auth_mvc.common.utils.constants.CommonConstants;
import com.david.auth_mvc.model.domain.dto.response.PairTokenResponse;
import com.david.auth_mvc.model.domain.entity.Credential;
import com.david.auth_mvc.model.domain.events.DomainEventPublisher;
import com.david.auth_mvc.model.domain.events.SendEmailEvent;
import com.david.auth_mvc.model.domain.events.UserRegisteredEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@AllArgsConstructor
public class UserRegisteredEventHandler {

    private final ITokenService tokenService;
    private final DomainEventPublisher domainEventPublisher;

    @EventListener
    public void handle(UserRegisteredEvent event) {
        Credential credential = event.getCredential();
        PairTokenResponse response = this.tokenService.generateVerifyAccountTokens(credential);

        domainEventPublisher.publish(new SendEmailEvent(credential, response.getAccessToken(), CommonConstants.TYPE_ACCESS_TOKEN_TO_VERIFY_ACCOUNT , response.getRefreshToken()));
    }
}
