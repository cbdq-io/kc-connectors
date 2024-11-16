package io.cbdq;

import org.apache.kafka.connect.sink.SinkTask;
import org.apache.kafka.connect.sink.SinkRecord;

import org.apache.qpid.jms.JmsConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.*;

public class AzureServiceBusSinkTask extends SinkTask {

    private static final Logger log = LoggerFactory.getLogger(AzureServiceBusSinkTask.class);

    private Map<String, MessageProducer> jmsProducers;
    private Connection jmsConnection;
    private Session jmsSession;
    private AzureServiceBusSinkConnectorConfig config;

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting a task in version {} of the connector.", VersionUtil.getVersion());
        config = new AzureServiceBusSinkConnectorConfig(props);

        // Retrieve the connection string as a Password type
        String connectionString = config.getPassword(AzureServiceBusSinkConnectorConfig.CONNECTION_STRING_CONFIG).value();

        try {
            // Parse connection string for JMS parameters
            String brokerURL = parseBrokerURL(connectionString);
            String username = parseUsername(connectionString);
            String password = parsePassword(connectionString);

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
                        Destination destination = jmsSession.createTopic(topic);
                        MessageProducer producer = jmsSession.createProducer(destination);
                        jmsProducers.put(topic, producer);
                        log.info("Initialized JMS producer for topic: {}", topic);
                    }
                }
            } else {
                log.error("No topics specified in the configuration");
            }

            jmsConnection.start();
        } catch (JMSException e) {
            throw new RuntimeException("Failed to initialize JMS client", e);
        }
    }

    @Override
    public void put(Collection<SinkRecord> envelopes) {
        log.info("Received {} records", envelopes.size());
        for (SinkRecord envelope : envelopes) {
            processRecord(envelope);
        }
    }

    private synchronized void reconnect() {
        log.warn("Attempting to reconnect to Azure Service Bus...");
        try {
            // Close existing resources
            if (jmsSession != null) {
                jmsSession.close();
            }
            if (jmsConnection != null) {
                jmsConnection.close();
            }

            // Reinitialize connection and session
            String connectionString = config.getPassword(AzureServiceBusSinkConnectorConfig.CONNECTION_STRING_CONFIG).value();
            String brokerURL = parseBrokerURL(connectionString);
            String username = parseUsername(connectionString);
            String password = parsePassword(connectionString);

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
                        Destination destination = jmsSession.createTopic(topic);
                        MessageProducer producer = jmsSession.createProducer(destination);
                        jmsProducers.put(topic, producer);
                        log.info("Reconnected and initialized JMS producer for topic: {}", topic);
                    }
                }
            }

            jmsConnection.start();
            log.info("Reconnection successful.");
        } catch (Exception e) {
            throw new RuntimeException("Reconnection failed", e);
        }
    }

    private void processRecord(SinkRecord envelope) {
        String kafkaTopic = envelope.topic();
        MessageProducer producer = jmsProducers.get(kafkaTopic);

        if (producer == null) {
            log.warn("No JMS producer found for topic {}", kafkaTopic);
            return;
        }

        int maxAttempts = config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_MAX_ATTEMPTS_CONFIG);
        int waitTimeMs = config.getInt(AzureServiceBusSinkConnectorConfig.RETRY_WAIT_TIME_MS_CONFIG);

        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                Message message;
                if (envelope.value() instanceof byte[] data) {
                    BytesMessage bytesMessage = jmsSession.createBytesMessage();
                    bytesMessage.writeBytes(data);
                    message = bytesMessage;
                } else if (envelope.value() instanceof String string) {
                    message = jmsSession.createTextMessage(string);
                } else {
                    log.error("Unsupported record value type: {}", envelope.value().getClass());
                    return;
                }

                producer.send(message);
                return; // Exit on successful send

            } catch (JMSException e) {
                attempt++;
                log.warn("Attempt {} failed to send message to topic {}. Retrying in {} ms...", attempt, kafkaTopic, waitTimeMs, e);

                if (attempt >= maxAttempts || e instanceof javax.jms.IllegalStateException) {
                    log.error("Session or producer error detected. Triggering recovery.");
                    reconnect();
                }

                try {
                    Thread.sleep(waitTimeMs);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during backoff wait", interruptedException);
                }
            }
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

    // Parsing methods for JMS connection parameters
    private String parseBrokerURL(String connectionString) {
        String brokerURL;
        String endpoint = null;
        String protocol = null;

        String[] parts = connectionString.split(";");

        for (String part : parts) {
            if (part.startsWith("Endpoint=sb://")) {
                endpoint = part.substring("Endpoint=sb://".length());
                protocol = "amqps://";
            } else if (part.startsWith("Endpoint=amqp://")) {
                endpoint = part.substring("Endpoint=amqp://".length());
                protocol = "amqp://";
            }
        }

        if (endpoint == null) {
            throw new IllegalArgumentException("No endpoint found in the Azure Service Bus connection string.");
        }

        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        brokerURL = protocol + endpoint;
        log.info("Broker URL parsed as '{}'.", brokerURL);
        return brokerURL;
    }

    private String parseUsername(String connectionString) {
        String username = "";
        String[] parts = connectionString.split(";");
        for (String part : parts) {
            if (part.startsWith("SharedAccessKeyName=")) {
                username = part.substring("SharedAccessKeyName=".length());
            }
        }
        return username;
    }

    private String parsePassword(String connectionString) {
        String password = "";
        String[] parts = connectionString.split(";");
        for (String part : parts) {
            if (part.startsWith("SharedAccessKey=")) {
                password = part.substring("SharedAccessKey=".length());
            }
        }
        return password;
    }
}
