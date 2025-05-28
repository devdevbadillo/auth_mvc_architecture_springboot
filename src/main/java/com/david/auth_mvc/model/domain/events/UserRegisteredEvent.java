package com.david.auth_mvc.model.domain.events;

import com.david.auth_mvc.model.domain.entity.Credential;
import lombok.Getter;

@Getter
public class UserRegisteredEvent extends DomainEvent {

    private final Credential credential;

    public UserRegisteredEvent(Credential credential) {
        super();
        this.credential = credential;
    }
}
