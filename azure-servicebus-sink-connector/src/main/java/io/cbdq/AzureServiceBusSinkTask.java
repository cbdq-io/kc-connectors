package io.cbdq;

import com.azure.messaging.servicebus.*;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class AzureServiceBusSinkTask extends SinkTask {

    private static final Logger log = LoggerFactory.getLogger(AzureServiceBusSinkTask.class);

    /* package-private */ Map<String, ServiceBusSenderClient> serviceBusSenders;
    private AzureServiceBusSinkConnectorConfig config;
    private PrometheusMetrics metrics;
    private TopicRenameFormat renamer;
    /* package-private */ boolean setKafkaPartitionAsSessionId;
    private String connectionString;

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting task in version {} of the connector.", VersionUtil.getVersion());

        config = new AzureServiceBusSinkConnectorConfig(props);
        renamer = new TopicRenameFormat(
            config.getString(AzureServiceBusSinkConnectorConfig.TOPIC_RENAME_FORMAT_CONFIG)
        );
        setKafkaPartitionAsSessionId = config.getBoolean(AzureServiceBusSinkConnectorConfig.SET_KAFKA_PARTITION_AS_SESSION_ID_CONFIG);
        connectionString = config.getPassword(AzureServiceBusSinkConnectorConfig.CONNECTION_STRING_CONFIG).value();

        serviceBusSenders = new HashMap<>();
        String topicsStr = props.get("topics");

        if (topicsStr != null) {
            ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder()
                .connectionString(connectionString);

            for (String topic : topicsStr.split(",")) {
                topic = topic.trim();
                if (!topic.isEmpty()) {
                    String destinationTopic = renamer.rename(topic);
                    ServiceBusSenderClient sender = clientBuilder
                        .sender()
                        .topicName(destinationTopic)
                        .buildClient();
                    serviceBusSenders.put(topic, sender);
                    log.info("Initialized Azure Service Bus sender for topic: {} -> {}", topic, destinationTopic);
                }
            }
        } else {
            log.error("No topics specified in the configuration");
        }

        JvmMetrics.builder().register();  // JVM metrics for Prometheus
        String connectorName = context.configs().get("name").toLowerCase();
        metrics = PrometheusMetrics.getInstance(connectorName);
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        log.info("Received {} records", records.size());

        for (SinkRecord record : records) {
            processRecord(record);
            metrics.incrementMessageCounter();
        }
    }

    /* package-private */ void processRecord(SinkRecord record) {
        String kafkaTopic = record.topic();
        ServiceBusSenderClient sender = serviceBusSenders.get(kafkaTopic);

        if (sender == null) {
            log.warn("No Service Bus sender found for topic {}", kafkaTopic);
            return;
        }

        try {
            ServiceBusMessage message = createMessageFromRecord(record);
            sendWithRetry(sender, message, kafkaTopic, record);
        } catch (Exception e) {
            log.error("Failed to process record for topic {}: {}", kafkaTopic, e.getMessage(), e);
            throw new AzureServiceBusSinkException("Message permanently failed", e);
        }
    }

    /* package-private */ ServiceBusMessage createMessageFromRecord(SinkRecord record) {
        byte[] body;

        if (record.value() instanceof byte[] bytes) {
            body = bytes;
        } else if (record.value() instanceof String string) {
            body = string.getBytes(StandardCharsets.UTF_8);
        } else {
            throw new AzureServiceBusSinkException("Unsupported record value type: " + record.value().getClass());
        }

        ServiceBusMessage message = new ServiceBusMessage(body);

        if (setKafkaPartitionAsSessionId && record.kafkaPartition() != null) {
            message.setSessionId(record.kafkaPartition().toString());
        }

        if (record.key() != null) {
            message.getApplicationProperties().put("__kafka_key", record.key().toString());
        }

        message.getApplicationProperties().put("__kafka_partition", record.kafkaPartition());

        return message;
    }

    /* package-private */ void sendWithRetry(ServiceBusSenderClient sender, ServiceBusMessage message, String kafkaTopic, SinkRecord record) {
        int maxAttempts = config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_MAX_ATTEMPTS_CONFIG);
        int waitTimeMs = config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_WAIT_TIME_MS_CONFIG);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                sender.sendMessage(message);
                return;
            } catch (Exception e) {
                log.warn("Attempt {} failed to send message to topic {}. Retrying in {} ms...", attempt, kafkaTopic, waitTimeMs, e);

                if (attempt >= maxAttempts) {
                    log.error("All {} attempts failed. Topic: {}, Partition: {}, Offset: {}",
                            maxAttempts, kafkaTopic, record.kafkaPartition(), record.kafkaOffset());
                    throw new AzureServiceBusSinkException(
                        String.format("Failed to send message after %d attempts", maxAttempts), e
                    );
                }

                try {
                    Thread.sleep(waitTimeMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AzureServiceBusSinkException("Interrupted during backoff wait", ie);
                }
            }
        }
    }

    @Override
    public void stop() {
        log.info("Stopping AzureServiceBusSinkTask");

        if (serviceBusSenders != null) {
            for (Map.Entry<String, ServiceBusSenderClient> entry : serviceBusSenders.entrySet()) {
                entry.getValue().close();
                log.info("Closed Service Bus sender for topic {}", entry.getKey());
            }
        }
    }

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }
}
