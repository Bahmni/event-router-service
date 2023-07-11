package org.bahmni.eventrouterservice.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static org.apache.camel.LoggingLevel.INFO;

@Component
@ConditionalOnProperty(name = "bahmni.activemqToGCP.route-enabled", havingValue = "true")
public class BahmniActiveMQToGCPTopicRoute extends RouteBuilder {

    private final RouteDescriptionLoader routeDescriptionLoader;
    private final String googlePubSubProjectId;
    private final String serviceName;
    private final ObjectMapper objectMapper;

    public BahmniActiveMQToGCPTopicRoute(CamelContext context,
                                         RouteDescriptionLoader routeDescriptionLoader,
                                         @Value("${google-pubsub.project-id}") String googlePubSubProjectId,
                                         @Value("${service.name}") String serviceName,
                                         ObjectMapper objectMapper) {
        super(context);
        this.routeDescriptionLoader = routeDescriptionLoader;
        this.googlePubSubProjectId = googlePubSubProjectId;
        this.serviceName = serviceName;
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure() throws Exception {

        routeDescriptionLoader.getRouteDescriptions().forEach(routeDescription -> {

            BahmniPayloadFilter bahmniPayloadFilter = new BahmniPayloadFilter(objectMapper, routeDescription);
            BahmniPayloadProcessor bahmniPayloadProcessor = new BahmniPayloadProcessor(objectMapper, routeDescription);

            from("activemq:topic:" + routeDescription.getSource().getTopic().getName()+"?durableSubscriptionName="+routeDescription.getSource().getTopic().getName()+"&clientId="+serviceName)
                .log(INFO, "Received message from ActiveMQ with headers : ${headers}")
                .onException(Exception.class)
                    .handled(true)
                    .log(INFO, "Error while processing message from ActiveMQ topic : " + routeDescription.getSource().getTopic().getName())
                    .useOriginalMessage()
                    .redeliveryDelay(routeDescription.getErrorDestination().getRetryDeliveryDelayInMills())
                    .maximumRedeliveries(routeDescription.getErrorDestination().getMaxRetryDelivery())
                    .to("activemq:queue:" + routeDescription.getErrorDestination().getQueue().getName())
                    .log("Message sent to ActiveMQ failed message queue: "+routeDescription.getErrorDestination().getQueue().getName())
                .end()
                .filter(bahmniPayloadFilter)
                .process(bahmniPayloadProcessor)
                .toD("google-pubsub:"+googlePubSubProjectId+":"+"${headers.destination}")
                .log("Message sent to Google PubSub on topic : "+"${headers.destination}");
        });
    }
}