package org.bahmni.eventrouterservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader.Destination;
import org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader.RouteDescription;
import org.bahmni.eventrouterservice.exception.NoDestinationConfiguredForEventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader.BahmniEventType.BAHMNI_PATIENT_CREATED;
import static org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader.BahmniEventType.BAHMNI_PATIENT_UPDATED;

public class RouteDescriptionLoaderTest {

    @Test
    public void givenDestinationConfiguredForBahmniPatientCreatedEvent_whenFetchDestination_returnDestination() {

        String configFileLocation = "src/test/resources/route-descriptions.json";
        RouteDescriptionLoader routeDescriptionLoader = new RouteDescriptionLoader(configFileLocation, new ObjectMapper());

        RouteDescription routeDescription = routeDescriptionLoader.getRouteDescriptions().get(0);

        Destination destinationBasedOn = routeDescription.getDestinationBasedOn(BAHMNI_PATIENT_CREATED.name());

        Assertions.assertEquals("patient-registration", destinationBasedOn.getTopic().getName());
        Assertions.assertEquals(BAHMNI_PATIENT_CREATED, destinationBasedOn.getOnEventType());
    }

    @Test
    public void givenInvalidEventType_whenFetchDestination_throwIllegalArgumentException() {

        String configFileLocation = "src/test/resources/route-descriptions.json";
        RouteDescriptionLoader routeDescriptionLoader = new RouteDescriptionLoader(configFileLocation, new ObjectMapper());

        RouteDescription routeDescription = routeDescriptionLoader.getRouteDescriptions().get(0);

        Assertions.assertThrows(IllegalArgumentException.class, () -> routeDescription.getDestinationBasedOn("Invalid-name"));
    }

    @Test
    public void givenDestinationNotConfiguredForBahmniPatientUpdatedEvent_whenFetchDestination_throwNoDestinationConfiguredForEventTypeException() {

        String configFileLocation = "src/test/resources/route-descriptions.json";
        RouteDescriptionLoader routeDescriptionLoader = new RouteDescriptionLoader(configFileLocation, new ObjectMapper());

        RouteDescription routeDescription = routeDescriptionLoader.getRouteDescriptions().get(0);

        NoDestinationConfiguredForEventType exception = Assertions.assertThrows(NoDestinationConfiguredForEventType.class, () -> routeDescription.getDestinationBasedOn(BAHMNI_PATIENT_UPDATED.name()));

        Assertions.assertEquals("No destination configured for event type : "+BAHMNI_PATIENT_UPDATED.name(), exception.getMessage());
    }
}