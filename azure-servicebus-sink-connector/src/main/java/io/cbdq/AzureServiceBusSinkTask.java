package io.cbdq;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryMode;
import java.time.Duration;
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
    /* package-private */ boolean setKafkaPartitionAsSessionId;

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting task in version {} of the connector.", VersionUtil.getVersion());

        config = new AzureServiceBusSinkConnectorConfig(props);
        TopicRenameFormat renamer = new TopicRenameFormat(config.getString(AzureServiceBusSinkConnectorConfig.TOPIC_RENAME_FORMAT_CONFIG));
        setKafkaPartitionAsSessionId = config.getBoolean(AzureServiceBusSinkConnectorConfig.SET_KAFKA_PARTITION_AS_SESSION_ID_CONFIG);

        String connectionString = config.getPassword(AzureServiceBusSinkConnectorConfig.CONNECTION_STRING_CONFIG).value();

        AmqpRetryOptions retryOptions = new AmqpRetryOptions()
        .setMaxRetries(config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_MAX_ATTEMPTS_CONFIG))
        .setDelay(Duration.ofMillis(config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_DELAY_MS_CONFIG)))
        .setMaxDelay(Duration.ofMillis(config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_MAX_DELAY_MS_CONFIG)))
        .setTryTimeout(Duration.ofMillis(config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_TOTAL_TIMEOUT_MS_CONFIG)))
        .setMode("fixed".equalsIgnoreCase(config.getString(AzureServiceBusSinkConnectorConfig.RETRY_MODE_CONFIG))
                 ? AmqpRetryMode.FIXED
                 : AmqpRetryMode.EXPONENTIAL);

        serviceBusSenders = new HashMap<>();
        String topicsStr = props.get("topics");

        if (topicsStr != null) {
            ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .retryOptions(retryOptions);

            for (String topic : topicsStr.split(",")) {
                topic = topic.trim();
                if (!topic.isEmpty()) {
                    String destinationTopic = renamer.rename(topic);
                    ServiceBusSenderClient sender = clientBuilder.sender().topicName(destinationTopic).buildClient();
                    serviceBusSenders.put(topic, sender);
                    log.info("Initialized Azure Service Bus sender for topic: {} -> {}", topic, destinationTopic);
                }
            }
        } else {
            log.error("No topics specified in the configuration");
        }

        JvmMetrics.builder().register();
        metrics = PrometheusMetrics.getInstance(context.configs().get("name").toLowerCase());
    }

    @Override
    public void put(Collection<SinkRecord> envelopes) {
        if (envelopes == null || envelopes.isEmpty()) {
            log.info("Received empty record batch, skipping.");
            return;
        }

        log.info("Received {} records", envelopes.size());
        Map<String, List<SinkRecord>> recordsByTopic = groupRecordsByTopic(envelopes);

        for (Map.Entry<String, List<SinkRecord>> entry : recordsByTopic.entrySet()) {
            sendMessages(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, List<SinkRecord>> groupRecordsByTopic(Collection<SinkRecord> envelopes) {
        Map<String, List<SinkRecord>> grouped = new HashMap<>();
        for (SinkRecord envelope : envelopes) {
            grouped.computeIfAbsent(envelope.topic(), k -> new ArrayList<>()).add(envelope);
            metrics.incrementMessageCounter();
        }
        return grouped;
    }

    private void sendMessages(String topic, List<SinkRecord> records) {
        int largeThresholdBytes = config.getInt(AzureServiceBusSinkConnectorConfig.LARGE_MESSAGE_THRESHOLD_BYTES_CONFIG);

        for (SinkRecord record : records) {
            ServiceBusMessage probe = createMessageFromRecord(record);

            if (probe.getBody().getLength() > largeThresholdBytes) {
                log.warn("Large message detected ({} bytes) — sending all records individually for topic {}",
                        probe.getBody().getLength(), topic);

                for (SinkRecord r : records) {
                    ServiceBusMessage m = createMessageFromRecord(r);
                    ServiceBusSenderClient sender = serviceBusSenders.get(topic);

                    if (sender == null) {
                        throw new AzureServiceBusSinkException("No sender configured for topic: " + topic);
                    }

                    ServiceBusMessageBatch soloBatch = sender.createMessageBatch();
                    if (!soloBatch.tryAddMessage(m)) {
                        throw new AzureServiceBusSinkException(
                            String.format("Message too large to fit in batch for topic '%s'", topic)
                        );
                    }

                    sender.sendMessages(soloBatch);
                }
                return;
            }
        }

        // Proceed with batching if no large messages
        sendBatchToTopic(topic, records);
    }

    private void sendBatchToTopic(String topic, List<SinkRecord> records) {
        ServiceBusSenderClient sender = serviceBusSenders.get(topic);

        if (sender == null) {
            throw new AzureServiceBusSinkException("No sender configured for topic: " + topic);
        }

        try {
            ServiceBusMessageBatch batch = sender.createMessageBatch();

            for (SinkRecord envelope : records) {
                ServiceBusMessage msg = createMessageFromRecord(envelope);

                boolean added = batch.tryAddMessage(msg);
                if (!added) {
                    if (batch.getCount() > 0) {
                        log.info("Sending full batch of {} messages to topic {}", batch.getCount(), topic);
                        sender.sendMessages(batch);
                    } else {
                        log.warn("Batch rejected first message — skipping send and creating a new batch");
                    }

                    batch = sender.createMessageBatch();

                    boolean addedToNew = batch.tryAddMessage(msg);
                    if (!addedToNew) {
                        throw new AzureServiceBusSinkException(
                            "Single message too large to fit in an empty batch for topic: " + topic
                        );
                    }
                }
            }

            if (batch.getCount() > 0) {
                log.info("Sending final batch of {} messages to topic {}", batch.getCount(), topic);
                sender.sendMessages(batch);
            } else {
                log.debug("No messages to send in final batch for topic {}", topic);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to send messages to topic '%s': %s", topic, e.getMessage());
            throw new AzureServiceBusSinkException(errorMessage, e);
        }
    }

    /* package-private */ ServiceBusMessage createMessageFromRecord(SinkRecord envelope) {
        byte[] body;

        if (envelope.value() instanceof byte[] bytes) {
            body = bytes;
        } else if (envelope.value() instanceof String string) {
            body = string.getBytes(StandardCharsets.UTF_8);
        } else {
            throw new AzureServiceBusSinkException("Unsupported record value type: " + envelope.value().getClass());
        }

        ServiceBusMessage message = new ServiceBusMessage(body);

        if (setKafkaPartitionAsSessionId && envelope.kafkaPartition() != null) {
            message.setSessionId(envelope.kafkaPartition().toString());
        }

        if (envelope.key() != null) {
            message.getApplicationProperties().put("__kafka_key", envelope.key().toString());
        }

        message.getApplicationProperties().put("__kafka_partition", envelope.kafkaPartition());

        return message;
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
