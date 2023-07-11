package org.bahmni.eventrouterservice.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.routepolicy.quartz.CronScheduledRoutePolicy;
import org.bahmni.eventrouterservice.configuration.RouteDescriptionLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static org.apache.camel.LoggingLevel.INFO;

@Component
@ConditionalOnProperty(name = "bahmni.activemqToGCP.failed-route-enabled", havingValue = "true")
public class BahmniActiveMQToGCPTopicFailedRoute extends RouteBuilder {

    private final RouteDescriptionLoader routeDescriptionLoader;
    private final String googlePubSubProjectId;
    private final ObjectMapper objectMapper;

    public BahmniActiveMQToGCPTopicFailedRoute(CamelContext context,
                                               RouteDescriptionLoader routeDescriptionLoader,
                                               @Value("${google-pubsub.project-id}") String googlePubSubProjectId,
                                               ObjectMapper objectMapper) {
        super(context);
        this.routeDescriptionLoader = routeDescriptionLoader;
        this.googlePubSubProjectId = googlePubSubProjectId;
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure() throws Exception {

        System.out.println("googlePubSubProjectId : "+googlePubSubProjectId);

        routeDescriptionLoader.getRouteDescriptions().forEach(routeDescription -> {

            CronScheduledRoutePolicy cronScheduledRoutePolicy = new CronScheduledRoutePolicy();
            cronScheduledRoutePolicy.setRouteStartTime(routeDescription.getErrorDestination().getCronExpressionForRetryStart());
            cronScheduledRoutePolicy.setRouteStopTime(routeDescription.getErrorDestination().getCronExpressionForRetryStop());

            BahmniPayloadFilter bahmniPayloadPropertiesFilterPredicateFor = new BahmniPayloadFilter(objectMapper, routeDescription);
            BahmniPayloadProcessor bahmniPayloadProcessor = new BahmniPayloadProcessor(objectMapper, routeDescription);

            from("activemq:queue:" + routeDescription.getErrorDestination().getQueue().getName())
                .routePolicy(cronScheduledRoutePolicy)
                .noAutoStartup()
                .onException(Exception.class)
                    .handled(true)
                    .log(LoggingLevel.ERROR, "Following exception occurred : ${exception.message} for processing the payload : ${body}")
                .end()
                .log(INFO, "Received failed message from ActiveMQ queue : " + routeDescription.getErrorDestination().getQueue().getName())
                .filter(bahmniPayloadPropertiesFilterPredicateFor)
                .process(bahmniPayloadProcessor)
                .toD("google-pubsub:"+googlePubSubProjectId+":"+"${headers.destination}")
                .log("Message sent to Google PubSub on topic : "+"${headers.destination}");
        });
    }
}
