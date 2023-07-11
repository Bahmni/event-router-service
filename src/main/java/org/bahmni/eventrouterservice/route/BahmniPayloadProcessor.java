package org.bahmni.eventrouterservice.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.json.JsonObject;
import org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader.RouteDescription;

import java.util.LinkedHashMap;

@Slf4j
class BahmniPayloadProcessor implements Processor {

    private final ObjectMapper objectMapper;
    private final RouteDescription routeDescription;

    public BahmniPayloadProcessor(ObjectMapper objectMapper, RouteDescription routeDescription) {
        this.objectMapper = objectMapper;
        this.routeDescription = routeDescription;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        if(routeDescription.getAdditionalProperties().isEmpty()) {
            log.info("Empty additional properties");
            return;
        }
        String payloadAsJsonString = exchange.getIn().getBody(String.class);
        String updatedPayloadAsJson = addStaticData(payloadAsJsonString, routeDescription.getAdditionalProperties());
        exchange.getIn().setBody(updatedPayloadAsJson);

        String destinationTopic = getDestination(exchange.getIn().getHeader("eventType"));
        exchange.getIn().setHeader("destination", destinationTopic);
    }

    private String getDestination(Object eventType) {
        String eventTypeAsString = (String) eventType;
        return routeDescription.getDestinationBasedOn(eventTypeAsString).getTopic().getName();
    }

    private String addStaticData(String jsonBodyAsString, LinkedHashMap<String, String> additionalProperties) {
        try {
            JsonObject jsonObject = objectMapper.readValue(jsonBodyAsString, JsonObject.class);
            jsonObject.putAll(additionalProperties);
            log.info("Added additional properties to payload for uuid : "+jsonObject.get("uuid"));
            return jsonObject.toJson();
        } catch (JsonProcessingException exception) {
            log.info("Failed to process payload : " + exception.getMessage());
            throw new RuntimeException(exception);
        }
    }

    public enum EventHeaderKey {
        EVENT_TYPE("eventType"),
        PAYLOAD_ID("payloadId"),
        EVENT_ID("eventId"),
        PUBLISHED_DATE_TIME("publishedDateTime");

        private final String key;

        EventHeaderKey(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }
}
