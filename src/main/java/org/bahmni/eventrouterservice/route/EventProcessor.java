package org.bahmni.eventrouterservice.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader.RouteDescription;

import java.util.LinkedHashMap;

@Slf4j
class EventProcessor implements Processor {

    private final ObjectMapper objectMapper;
    private final RouteDescription routeDescription;

    public EventProcessor(ObjectMapper objectMapper, RouteDescription routeDescription) {
        this.objectMapper = objectMapper;
        this.routeDescription = routeDescription;
    }

    @Override
    public void process(Exchange exchange) {
        if(routeDescription.getAdditionalProperties().isEmpty()) {
            log.info("Empty additional properties");
            return;
        }
        String payloadAsJsonString = exchange.getIn().getBody(String.class);
        String updatedPayloadAsJson = addStaticData(payloadAsJsonString, routeDescription.getAdditionalProperties());
        exchange.getIn().setBody(updatedPayloadAsJson);

        String destinationTopic = getDestination(exchange.getIn().getHeader("eventType"));
        exchange.setProperty("destination", destinationTopic);
    }

    private String getDestination(Object eventType) {
        String eventTypeAsString = (String) eventType;
        return routeDescription.getDestinationBasedOn(eventTypeAsString).getTopic().getName();
    }

    private String addStaticData(String jsonBodyAsString, LinkedHashMap<String, String> additionalProperties) {
        try {
            ObjectNode objectNode = objectMapper.readValue(jsonBodyAsString, ObjectNode.class);
            additionalProperties.forEach(objectNode::put);
            log.info("Added additional properties to payload for uuid : "+objectNode.get("uuid"));
            return objectMapper.writeValueAsString(objectNode);
        } catch (JsonProcessingException exception) {
            log.info("Failed to process payload : " + exception.getMessage());
            throw new RuntimeException(exception);
        }
    }

}
