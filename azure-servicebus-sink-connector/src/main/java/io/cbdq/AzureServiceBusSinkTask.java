package io.cbdq;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import org.apache.kafka.common.config.types.Password;

import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.sink.SinkTask;
import org.apache.kafka.connect.sink.SinkRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;

public class AzureServiceBusSinkTask extends SinkTask {

    private static final Logger log = LoggerFactory.getLogger(AzureServiceBusSinkTask.class);

    private AzureServiceBusSinkConnectorConfig config;
    private Map<String, ServiceBusSenderClient> senderClients;

    @Override
    public void start(Map<String, String> props) {
        log.debug("k2sbus - Starting task {}", props);
        config = new AzureServiceBusSinkConnectorConfig(props);

        // Retrieve the connection string as a Password type
        Password connectionStringPassword = config.getPassword(AzureServiceBusSinkConnectorConfig.CONNECTION_STRING_CONFIG);
        String connectionString = connectionStringPassword == null ? "" : connectionStringPassword.value();

        // Check if we're using the development emulator
        boolean useDevelopmentEmulator = connectionString.contains("UseDevelopmentEmulator=true");

        ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder().connectionString(connectionString);

        if (useDevelopmentEmulator) {
            log.warn("k2sbus - UseDevelopmentEmulator=true detected.");
            clientBuilder = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .customEndpointAddress("https://localhost.localsandbox.sh:5672")
                .transportType(AmqpTransportType.AMQP);
        } else {
            log.info("k2sbus - Using standard SSL verification for Service Bus connection.");
            clientBuilder = new ServiceBusClientBuilder().connectionString(connectionString);
        }

        senderClients = new HashMap<>();

        // Get the list of topics from the configuration
        String topicsStr = props.get("topics");
        if (topicsStr != null) {
            List<String> topicList = Arrays.asList(topicsStr.split(","));
            for (String topic : topicList) {
                topic = topic.trim();
                if (!topic.isEmpty()) {
                    ServiceBusSenderClient senderClient = clientBuilder
                            .sender()
                            .topicName(topic)
                            .buildClient();
                    senderClients.put(topic, senderClient);
                    log.info("k2sbus - Initialized Service Bus sender client for topic: {}", topic);
                }
            }
        } else {
            log.error("k2sbus - No topics specified in the configuration");
        }
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        log.info("k2sbus - Received {} records", records.size());
        for (SinkRecord record : records) {
            processRecord(record);
        }
    }

    private void processRecord(SinkRecord record) {
        String kafkaTopic = record.topic();
        ServiceBusSenderClient senderClient = senderClients.get(kafkaTopic);

        if (senderClient == null) {
            log.warn("k2sbus - No sender client found for topic {}", kafkaTopic);
            return;
        }

        log.info("k2sbus - Processing record from topic: {}, partition: {}, offfset: {}",
                 record.topic(), record.kafkaPartition(), record.kafkaOffset());
        byte[] messageBody;

        if (record.value() instanceof byte[]) {
            messageBody = (byte[]) record.value();
        } else if (record.value() instanceof String) {
            messageBody = ((String) record.value()).getBytes(StandardCharsets.UTF_8);
        } else {
            log.error("k2sbus - Unsupported record value type: {}", record.value().getClass());
            return;
        }

        ServiceBusMessage message = new ServiceBusMessage(messageBody);

        int attempts = 0;
        boolean success = false;
        int maxRetryAttempts = config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_MAX_ATTEMPTS_CONFIG);
        long retryWaitTimeMs = config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_WAIT_TIME_MS_CONFIG);
        log.debug("k2sbus - Sending message to Service Bus topic: {}", kafkaTopic);

        while (attempts < maxRetryAttempts && !success) {
            try {
                senderClient.sendMessage(message);
                success = true;
                log.debug("k2sbus - Successfully sent message to Service Bus topic {}", kafkaTopic);
            } catch (Exception e) {
                attempts++;
                log.error("k2sbus - Failed to send message to Service Bus topic {} on attempt {}: {}", kafkaTopic, attempts, e.getMessage());
                if (attempts < maxRetryAttempts) {
                    try {
                        Thread.sleep(retryWaitTimeMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    throw new RetriableException("Failed to send message after " + maxRetryAttempts + " attempts", e);
                }
            }
        }
    }

    @Override
    public void stop() {
        log.info("k2sbus - Stopping AzureServiceBusSinkTask");
        for (Map.Entry<String, ServiceBusSenderClient> entry : senderClients.entrySet()) {
            try {
                entry.getValue().close();
                log.info("k2sbus - Closed sender client for topic {}", entry.getKey());
            } catch (Exception e) {
                log.error("k2sbus - Error closing sender client for topic {}: {}", entry.getKey(), e.getMessage());
            }
        }
    }

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }
}
