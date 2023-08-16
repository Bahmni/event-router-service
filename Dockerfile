FROM amazoncorretto:17 as builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} event-router-service.jar
RUN java -Djarmode=layertools -jar event-router-service.jar extract


FROM amazoncorretto:17
RUN yum update -y && \
    yum install -y tzdata && \
    yum clean all
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]