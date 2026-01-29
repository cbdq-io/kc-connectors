FROM confluentinc/cp-kafka-connect:8.1.1

LABEL org.opencontainers.image.description="A Kafka Connect Sink Connecter for Azure Service Bus."

USER root

# hadolint ignore=DL3013,DL3041
RUN microdnf clean all \
  && microdnf install -y bind-utils jq python3-pip \
  && microdnf upgrade -y \
    gnupg2 \
    libpng \
    openssl-libs \
  && microdnf clean all \
  && python -m pip install --no-cache-dir prometheus-client requests

COPY --chmod=0755 --chown=root:root kccinit.py /usr/local/bin/kccinit.py
COPY --chmod=0755 --chown=root:root kcstatus /usr/local/bin/kcstatus

USER appuser

RUN mkdir /home/appuser/connectors

COPY --chown=appuser:appuser ./azure-servicebus-sink-connector/target/azure-servicebus-sink-connector-*.jar /home/appuser/connectors

ENV CONNECT_PLUGIN_PATH=/usr/share/java/,/usr/share/confluent-hub-components/,/home/appuser/connectors/
HEALTHCHECK CMD curl --fail --silent localhost:8083/connectors || exit 1
