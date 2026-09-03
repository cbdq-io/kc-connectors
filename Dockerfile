FROM registry.access.redhat.com/ubi9/ubi:9.8 AS packages

# hadolint ignore=DL3013,DL3041
RUN dnf install -y \
      python3.14 \
      python3.14-pip \
    && mkdir /microdir \
    && dnf install -y \
      --installroot=/microdir \
      --releasever=9.8 \
      --setopt=install_weak_deps=false \
      --setopt=tsflags=nodocs \
      bind-utils \
      curl \
      jq \
      python3.14 \
    && dnf clean all \
      --installroot=/microdir \
    && rm -rf /microdir/var/cache/dnf \
    && python3.14 -m pip install \
      --no-cache-dir \
      --target=/microdir/usr/lib/python3.14/site-packages \
      prometheus-client \
      requests \
    && ln -s python3.14 /microdir/usr/bin/python3 \
    && ln -s python3.14 /microdir/usr/bin/python \
    && rm -f \
      /microdir/etc/passwd \
      /microdir/etc/group \
      /microdir/etc/shadow \
      /microdir/etc/gshadow


FROM confluentinc/cp-kafka-connect:8.3.1

LABEL org.opencontainers.image.description="A Kafka Connect Sink Connector for Azure Service Bus."

USER 0

COPY --from=packages /microdir/ /
COPY --chmod=0755 --chown=root:root kccinit.py /usr/local/bin/kccinit.py
COPY --chmod=0755 --chown=root:root kcstatus /usr/local/bin/kcstatus

USER 1000

RUN mkdir /home/appuser/connectors

COPY --chown=1000:1000 \
  ./azure-servicebus-sink-connector/target/azure-servicebus-sink-connector-*.jar \
  /home/appuser/connectors

ENV CONNECT_PLUGIN_PATH=/usr/share/java/,/usr/share/confluent-hub-components/,/home/appuser/connectors/

# hadolint ignore=DL3025
HEALTHCHECK CMD curl --fail --silent localhost:8083/connectors || exit 1
