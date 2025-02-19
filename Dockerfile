FROM confluentinc/cp-kafka-connect:7.9.0

LABEL org.opencontainers.image.description "A Kafka Connect Sink Connecter for Azure Service Bus."

USER root

RUN dnf clean all \
  && dnf upgrade -y krb5-libs pam python3-unbound unbound-libs \
  && dnf install -y bind-utils

COPY --chmod=0755 --chown=root:root kccinit.py /usr/local/bin/kccinit.py

USER appuser

RUN mkdir /home/appuser/connectors

COPY --chown=appuser:appuser ./azure-servicebus-sink-connector/target/azure-servicebus-sink-connector-*.jar /home/appuser/connectors

ENV CONNECT_PLUGIN_PATH=/usr/share/java/,/usr/share/confluent-hub-components/,/home/appuser/connectors/
HEALTHCHECK CMD curl --fail --silent localhost:8083/connectors || exit 1
