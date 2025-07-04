FROM confluentinc/cp-kafka-connect:7.9.2

LABEL org.opencontainers.image.description "A Kafka Connect Sink Connecter for Azure Service Bus."

USER root

# hadolint ignore=DL3041
RUN dnf clean all \
  && dnf upgrade -y freetype krb5-libs pam python3-unbound unbound-libs \
  && dnf install -y bind-utils jq \
  && dnf clean all \
  && python -m pip install --no-cache-dir prometheus-client==0.21.1

COPY --chmod=0755 --chown=root:root kccinit.py /usr/local/bin/kccinit.py
COPY --chmod=0755 --chown=root:root kcstatus /usr/local/bin/kcstatus

USER appuser

RUN mkdir /home/appuser/connectors

COPY --chown=appuser:appuser ./azure-servicebus-sink-connector/target/azure-servicebus-sink-connector-*.jar /home/appuser/connectors

ENV CONNECT_PLUGIN_PATH=/usr/share/java/,/usr/share/confluent-hub-components/,/home/appuser/connectors/
HEALTHCHECK CMD curl --fail --silent localhost:8083/connectors || exit 1
