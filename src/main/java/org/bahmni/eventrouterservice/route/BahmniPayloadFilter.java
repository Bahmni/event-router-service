package org.bahmni.eventrouterservice.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.util.json.JsonObject;
import org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader.RouteDescription;

import java.util.Map;

@Slf4j
class BahmniPayloadFilter implements Predicate {
    private final ObjectMapper objectMapper;
    private final RouteDescription routeDescription;

    public BahmniPayloadFilter(ObjectMapper objectMapper, RouteDescription routeDescription) {
        this.objectMapper = objectMapper;
        this.routeDescription = routeDescription;
    }

    @Override
    public boolean matches(Exchange exchange) {
        log.info("Checking Filters conditions : " + routeDescription.getFilterOnProperties());
        if(routeDescription.getFilterOnProperties().isEmpty()) {
            log.info("Empty Filters conditions");
            return true;
        }

        boolean matches = true;

        try {
            JsonObject payload = objectMapper.readValue(exchange.getIn().getBody(String.class), JsonObject.class);
            for (Map.Entry<String, String> filterCondition : routeDescription.getFilterOnProperties().entrySet()) {
                matches = matches && payload.containsKey(filterCondition.getKey()) && payload.get(filterCondition.getKey()).equals(filterCondition.getValue());
            }
            log.info("Filters conditions matches : " + matches +" for uuid : "+payload.get("uuid"));
            return matches;
        } catch (JsonProcessingException exception) {
            log.info("Failed to process payload : " + exception.getMessage());
            throw new RuntimeException(exception);
        }
    }
}
