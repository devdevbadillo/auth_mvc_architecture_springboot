package com.david.auth_mvc.model.domain.events;


public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
