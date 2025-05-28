package com.david.auth_mvc.model.domain.events;

import java.time.LocalDateTime;

public class DomainEvent {

    private final LocalDateTime occurredOn;

    public DomainEvent() {
        this.occurredOn = LocalDateTime.now();
    }

    public LocalDateTime occurredOn() {
        return this.occurredOn;
    }

}
