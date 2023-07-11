package org.bahmni.eventrouterservice.exception;

import org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader;

public class NoDestinationConfiguredForEventType extends RuntimeException {
    public NoDestinationConfiguredForEventType(RouteDescriptionLoader.BahmniEventType bahmniEventType) {
        super("No destination configured for event type : " + bahmniEventType.name());
    }
}
