package io.cbdq;

import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import org.apache.kafka.connect.sink.SinkTask;
import org.apache.kafka.connect.sink.SinkRecord;

import org.apache.qpid.jms.JmsConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.*;

import java.util.*;


public class AzureServiceBusSinkTask extends SinkTask {

    private static final Logger log = LoggerFactory.getLogger(AzureServiceBusSinkTask.class);

    private Map<String, MessageProducer> jmsProducers;
    private Connection jmsConnection;
    private Session jmsSession;
    private AzureServiceBusSinkConnectorConfig config;
    private String brokerURL;
    private String username;
    private String password;
    private PrometheusMetrics metrics;
    private TopicRenameFormat renamer;
    private boolean setKafkaPartitionAsSessionId;

    private Message createMessageFromRecord(SinkRecord envelope) throws JMSException {
        Message message;

        if (envelope.value() instanceof byte[] data) {
            BytesMessage bytesMessage = jmsSession.createBytesMessage();
            bytesMessage.writeBytes(data);
            message = bytesMessage;
        } else if (envelope.value() instanceof String string) {
            message = jmsSession.createTextMessage(string);
        } else {
            throw new AzureServiceBusSinkException("Unsupported record value type: " + envelope.value().getClass());
        }

        if (setKafkaPartitionAsSessionId && envelope.kafkaPartition() != null) {
            message.setStringProperty("JMSXGroupID", Integer.toString(envelope.kafkaPartition()));
        }

        if (envelope.key() != null) {
            message.setStringProperty("__kafka_key", envelope.key().toString());
        }

        message.setStringProperty("__kafka_partition", Integer.toString(envelope.kafkaPartition()));

        return message;
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting a task in version {} of the connector.", VersionUtil.getVersion());
        config = new AzureServiceBusSinkConnectorConfig(props);
        renamer = new TopicRenameFormat(
            config.getString(AzureServiceBusSinkConnectorConfig.TOPIC_RENAME_FORMAT_CONFIG)
        );
        setKafkaPartitionAsSessionId = config.getBoolean(AzureServiceBusSinkConnectorConfig.SET_KAFKA_PARTITION_AS_SESSION_ID_CONFIG).booleanValue();

        // Retrieve the connection string as a Password type
        String connectionString = config.getPassword(AzureServiceBusSinkConnectorConfig.CONNECTION_STRING_CONFIG).value();

        try {
            // Parse connection string for JMS parameters
            ConnectionStringParser parser = new ConnectionStringParser(connectionString);
            brokerURL = parser.getBrokerURL();
            log.debug("brokerURL {}", brokerURL);
            username = parser.getUserName();
            log.debug("userName {}", username);
            password = parser.getPassword();

            // Create JMS ConnectionFactory
            JmsConnectionFactory factory = new JmsConnectionFactory(username, password, brokerURL);

            // Create JMS Connection and Session
            jmsConnection = factory.createConnection();
            jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            jmsProducers = new HashMap<>();

            // Get the list of topics from the configuration
            String topicsStr = props.get("topics");
            if (topicsStr != null) {
                List<String> topicList = Arrays.asList(topicsStr.split(","));
                for (String topic : topicList) {
                    topic = topic.trim();

                    if (!topic.isEmpty()) {
                        String destinationTopic = renamer.rename(topic);
                        Destination destination = jmsSession.createTopic(
                            destinationTopic
                        );
                        MessageProducer producer = jmsSession.createProducer(destination);
                        jmsProducers.put(topic, producer);
                        log.info("Initialized JMS producer for topic: {} -> {}", topic, destinationTopic);
                    }
                }
            } else {
                log.error("No topics specified in the configuration");
            }

            jmsConnection.start();

            JvmMetrics.builder().register();  // Initialise the out-of-th-box JVM metrics.
            String connectorName = context.configs().get("name").toLowerCase();
            metrics = PrometheusMetrics.getInstance(connectorName);
        } catch (JMSException e) {
            throw new AzureServiceBusSinkException("Failed to initialise JMS client", e);
        }
    }

    @Override
    public void put(Collection<SinkRecord> envelopes) {
        log.info("Received {} records", envelopes.size());
        for (SinkRecord envelope : envelopes) {
            processRecord(envelope);
            metrics.incrementMessageCounter();
        }
    }

    private synchronized void reconnect() {
        log.warn("Attempting to reconnect to Azure Service Bus...");
        renamer = new TopicRenameFormat(
            config.getString(AzureServiceBusSinkConnectorConfig.TOPIC_RENAME_FORMAT_CONFIG)
        );

        try {
            // Close existing resources
            if (jmsSession != null) {
                jmsSession.close();
            }
            if (jmsConnection != null) {
                jmsConnection.close();
            }

            // Reinitialize connection and session
            JmsConnectionFactory factory = new JmsConnectionFactory(username, password, brokerURL);
            jmsConnection = factory.createConnection();
            jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Recreate producers
            jmsProducers.clear();
            String topicsStr = (String) config.originals().get("topics");

            if (topicsStr != null) {
                for (String topic : topicsStr.split(",")) {
                    topic = topic.trim();

                    if (!topic.isEmpty()) {
                        String destinationTopic = renamer.rename(topic);
                        Destination destination = jmsSession.createTopic(destinationTopic);
                        MessageProducer producer = jmsSession.createProducer(destination);
                        jmsProducers.put(topic, producer);
                        log.info("Reconnected and initialized JMS producer for topic: {} ->", topic);
                    }
                }
            }

            jmsConnection.start();
            log.info("Reconnection successful.");
        } catch (Exception e) {
            throw new AzureServiceBusSinkException("Reconnection failed", e);
        }
    }

    private void sendWithRetry(MessageProducer producer, Message message, String kafkaTopic, SinkRecord envelope) {
        int maxAttempts = config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_MAX_ATTEMPTS_CONFIG);
        int waitTimeMs = config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_WAIT_TIME_MS_CONFIG);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                producer.send(message);
                return; // success
            } catch (JMSException e) {
                log.warn("Attempt {} failed to send message to topic {}. Retrying in {} ms...", attempt, kafkaTopic, waitTimeMs, e);

                if (e instanceof jakarta.jms.IllegalStateException) {
                    log.warn("Session or producer error detected. Triggering recovery.");
                    reconnect();
                }

                if (attempt >= maxAttempts) {
                    log.error("All {} attempts failed. Topic: {}, Partition: {}, Offset: {}",
                              maxAttempts, kafkaTopic, envelope.kafkaPartition(), envelope.kafkaOffset());
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

    private void processRecord(SinkRecord envelope) {
        String kafkaTopic = envelope.topic();
        MessageProducer producer = jmsProducers.get(kafkaTopic);

        if (producer == null) {
            log.warn("No JMS producer found for topic {}", kafkaTopic);
            return;
        }

        try {
            Message message = createMessageFromRecord(envelope);
            sendWithRetry(producer, message, kafkaTopic, envelope);
        } catch (Exception e) {
            log.error("Failed to process record for topic {}: {}", kafkaTopic, e.getMessage(), e);
            throw new AzureServiceBusSinkException("Message permanently failed", e);
        }
    }

    @Override
    public void stop() {
        log.info("Stopping AzureServiceBusSinkTask");

        if (jmsProducers != null) {
            try {
                for (Map.Entry<String, MessageProducer> entry : jmsProducers.entrySet()) {
                    entry.getValue().close();
                    log.info("Closed JMS producer for topic {}", entry.getKey());
                }
                if (jmsSession != null) {
                    jmsSession.close();
                }
                if (jmsConnection != null) {
                    jmsConnection.close();
                }
                log.info("Closed JMS connection and session");
            } catch (JMSException e) {
                log.error("Error closing JMS resources: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }
}
