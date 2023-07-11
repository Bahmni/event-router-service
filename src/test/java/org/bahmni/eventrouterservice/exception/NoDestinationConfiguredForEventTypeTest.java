package org.bahmni.eventrouterservice.exception;

import org.junit.jupiter.api.Test;

import static org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader.BahmniEventType.BAHMNI_PATIENT_CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NoDestinationConfiguredForEventTypeTest {

    @Test
    public void givenPublisherId_whenCreatingException_thenCreateExceptionMessageInExpectedFormat() {
        NoDestinationConfiguredForEventType exception = new NoDestinationConfiguredForEventType(BAHMNI_PATIENT_CREATED);
        assertEquals("No destination configured for event type : "+BAHMNI_PATIENT_CREATED.name(), exception.getMessage());
    }
}