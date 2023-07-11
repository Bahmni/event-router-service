package org.bahmni.eventrouterservice.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader;
import org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader.RouteDescription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BahmniPayloadFilterTest {

    @Test
    public void givenFilterPropertyKeyWithValuePresentInPayload_whenApplyFilters_thenShouldReturnTrue() {

        RouteDescription routeDescription = mock(RouteDescription.class);
        LinkedHashMap<String, String> filterOnProperties = new LinkedHashMap<>();
        filterOnProperties.put("uuid", "patientUuid");
        when(routeDescription.getFilterOnProperties()).thenReturn(filterOnProperties);

        BahmniPayloadFilter bahmniPayloadFilter = new BahmniPayloadFilter(new ObjectMapper(), routeDescription);

        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);
        when(exchange.getIn()).thenReturn(message);
        when(message.getBody(String.class)).thenReturn("{\"uuid\":\"patientUuid\"}");

        boolean matches = bahmniPayloadFilter.matches(exchange);

        Assertions.assertTrue(matches);
    }

    @Test
    public void givenFilterPropertyKeyNotPresentInPayload_whenApplyFilters_thenShouldReturnFalse() {

        RouteDescription routeDescription = mock(RouteDescription.class);
        LinkedHashMap<String, String> filterOnProperties = new LinkedHashMap<>();
        filterOnProperties.put("uuid1", "patientUuid");
        when(routeDescription.getFilterOnProperties()).thenReturn(filterOnProperties);

        BahmniPayloadFilter bahmniPayloadFilter = new BahmniPayloadFilter(new ObjectMapper(), routeDescription);

        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);
        when(exchange.getIn()).thenReturn(message);
        when(message.getBody(String.class)).thenReturn("{\"uuid\":\"patientUuid\"}");

        boolean matches = bahmniPayloadFilter.matches(exchange);

        Assertions.assertFalse(matches);
    }

    @Test
    public void givenFiltersAreEmpty_whenApplyFilters_thenShouldReturnTrue() {

        RouteDescription routeDescription = mock(RouteDescription.class);
        when(routeDescription.getFilterOnProperties()).thenReturn(Maps.newLinkedHashMap());

        BahmniPayloadFilter bahmniPayloadFilter = new BahmniPayloadFilter(new ObjectMapper(), routeDescription);

        Exchange exchange = mock(Exchange.class);

        boolean matches = bahmniPayloadFilter.matches(exchange);

        Assertions.assertTrue(matches);
    }

    @Test
    public void givenFilterPropertyKeyWithValuePresentInInvalidPayload_whenApplyFilters_thenShouldThrowRuntimeException() {

        RouteDescription routeDescription = mock(RouteDescription.class);
        LinkedHashMap<String, String> filterOnProperties = new LinkedHashMap<>();
        filterOnProperties.put("uuid", "patientUuid");
        when(routeDescription.getFilterOnProperties()).thenReturn(filterOnProperties);

        BahmniPayloadFilter bahmniPayloadFilter = new BahmniPayloadFilter(new ObjectMapper(), routeDescription);

        Exchange exchange = mock(Exchange.class);
        Message message = mock(Message.class);
        when(exchange.getIn()).thenReturn(message);
        when(message.getBody(String.class)).thenReturn("{uuid\":\"patientUuid\"}");

        assertThrows(RuntimeException.class, () -> bahmniPayloadFilter.matches(exchange));
    }
}