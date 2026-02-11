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
    private ServiceBusClientBuilder clientBuilder;
    private TopicRenameFormat renamer;
    private String connectionString;
    /* package-private */ Map<String, ServiceBusSenderClient> serviceBusSenders;
    private AzureServiceBusSinkConnectorConfig config;
    private PrometheusMetrics metrics;
    /* package-private */ boolean setKafkaPartitionAsSessionId;

    /* package-private */ ServiceBusMessage createMessageFromEnvelope(SinkRecord envelope) {
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

    private Map<String, List<SinkRecord>> groupRecordsByTopic(Collection<SinkRecord> envelopes) {
        Map<String, List<SinkRecord>> grouped = new HashMap<>();
        for (SinkRecord envelope : envelopes) {
            grouped.computeIfAbsent(envelope.topic(), k -> new ArrayList<>()).add(envelope);
            metrics.incrementMessageCounter();
        }
        return grouped;
    }

    @Override
    public void put(Collection<SinkRecord> envelopes) {
        if (envelopes == null || envelopes.isEmpty()) {
            log.info("Received empty record batch, skipping.");
            return;
        }

        log.info("Received {} records from Kafka.", envelopes.size());
        Map<String, List<SinkRecord>> recordsByTopic = groupRecordsByTopic(envelopes);

        for (Map.Entry<String, List<SinkRecord>> entry : recordsByTopic.entrySet()) {
            sendMessagesToServiceBusTopic(entry.getKey(), entry.getValue());
        }
    }

    private ServiceBusSenderClient recreateSender(String sourceTopic) {
        String destinationTopic = renamer.rename(sourceTopic);
        ServiceBusSenderClient old = serviceBusSenders.get(sourceTopic);
        try { if (old != null) old.close(); } catch (Exception ignore) {}

        ServiceBusSenderClient replacement =
            clientBuilder.sender().topicName(destinationTopic).buildClient();

        serviceBusSenders.put(sourceTopic, replacement);
        log.warn("Recreated Service Bus sender for topic: {} -> {}", sourceTopic, destinationTopic);
        return replacement;
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting task in version {} of the connector.", VersionUtil.getVersion());
        config = new AzureServiceBusSinkConnectorConfig(props);
        renamer = new TopicRenameFormat(config.getString(AzureServiceBusSinkConnectorConfig.TOPIC_RENAME_FORMAT_CONFIG));
        setKafkaPartitionAsSessionId = config.getBoolean(AzureServiceBusSinkConnectorConfig.SET_KAFKA_PARTITION_AS_SESSION_ID_CONFIG);
        connectionString = config.getPassword(AzureServiceBusSinkConnectorConfig.CONNECTION_STRING_CONFIG).value();
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
            clientBuilder = new ServiceBusClientBuilder()
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

    private ServiceBusSenderClient sendBatchToTopic(
        String sourceTopic,
        ServiceBusSenderClient sender,
        ServiceBusMessageBatch batch,
        List<ServiceBusMessage> messages) {

        if (batch.getCount() == 0) {
            // Avoid sending an empty batch.
            return sender;
        }

        try {
            log.info("Sending a batch of {} messages to...", batch.getCount());
            sender.sendMessages(batch);
            return sender;
        } catch (ServiceBusException ex) {
            log.warn(
                "Batch send failed for topic {}. Recreating sender and falling back to individual sends.",
                sourceTopic,
                ex
            );

            ServiceBusSenderClient newSender = recreateSender(sourceTopic);

            for (ServiceBusMessage message : messages) {
                newSender.sendMessage(message);
            }

            return newSender;
        }
    }

    private void sendMessagesToServiceBusTopic(String sourceTopic, List<SinkRecord> envelopes) {
        ServiceBusSenderClient sender = serviceBusSenders.get(sourceTopic);
        log.debug("Attempting to send {} messages from Kafka topic '{}'.", envelopes.size(), sourceTopic);

        if (sender == null) {
            throw new AzureServiceBusSinkException("No sender configured for topic: " + sourceTopic);
        }

        ServiceBusMessageBatch currentBatch = sender.createMessageBatch();
        List<ServiceBusMessage> currentMessages = new ArrayList<>();

        // We try to add as many messages as a batch can fit based on the maximum size and send to Service Bus when
        // the batch can hold no more messages. Create a new batch for next set of messages and repeat until all
        // messages are sent.
        for (SinkRecord envelope : envelopes) {
            ServiceBusMessage message = createMessageFromEnvelope(envelope);

            if (currentBatch.tryAddMessage(message)) {
                currentMessages.add(message);
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            sendBatchToTopic(sourceTopic, sender, currentBatch, currentMessages);
            currentBatch = sender.createMessageBatch();
            currentMessages = new ArrayList<>();

            // Add that message that we couldn't before.
            if (!currentBatch.tryAddMessage(message)) {
                int bodyBytes = message.getBody().toBytes().length;
                log.debug("Message is too large ({} bytes) for an empty batch.", bodyBytes);
                sender.sendMessage(message);
            } else {
                currentMessages.add(message);
            }
        }

        sendBatchToTopic(sourceTopic, sender, currentBatch, currentMessages);
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
