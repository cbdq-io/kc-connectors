FROM confluentinc/cp-kafka-connect:7.8.0

LABEL org.opencontainers.image.description "A Kafka Connect Sink Connecter for Azure Service Bus."

USER root

RUN yum clean all \
  && yum upgrade -y krb5-libs pam

USER appuser

RUN mkdir /home/appuser/connectors

COPY --chown=appuser:appuser ./azure-servicebus-sink-connector/target/azure-servicebus-sink-connector-*.jar /home/appuser/connectors

ENV CONNECT_PLUGIN_PATH=/usr/share/java/,/usr/share/confluent-hub-components/,/home/appuser/connectors/
