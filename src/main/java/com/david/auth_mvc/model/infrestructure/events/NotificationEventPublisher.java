package com.david.auth_mvc.model.infrestructure.events;

import com.david.auth_mvc.model.domain.events.DomainEvent;
import com.david.auth_mvc.model.domain.events.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public NotificationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
