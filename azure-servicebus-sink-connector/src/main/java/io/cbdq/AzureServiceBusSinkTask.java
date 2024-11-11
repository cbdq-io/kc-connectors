package io.cbdq;

import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.sink.SinkTask;
import org.apache.kafka.connect.sink.SinkRecord;

import org.apache.qpid.jms.JmsConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.*;

public class AzureServiceBusSinkTask extends SinkTask {

    private static final Logger log = LoggerFactory.getLogger(AzureServiceBusSinkTask.class);

    private AzureServiceBusSinkConnectorConfig config;
    private Map<String, MessageProducer> jmsProducers;
    private Connection jmsConnection;
    private Session jmsSession;

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting task with properties: {}", props);
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

    private void processRecord(SinkRecord envelope) {
        String kafkaTopic = envelope.topic();
        MessageProducer producer = jmsProducers.get(kafkaTopic);

        if (producer == null) {
            log.warn("No JMS producer found for topic {}", kafkaTopic);
            return;
        } else if (log.isDebugEnabled()) {
            log.debug("Processing record from topic: {}, partition: {}, offset: {}",
                    envelope.topic(), envelope.kafkaPartition(), envelope.kafkaOffset());
        }

        try {
            Message message;

            if (envelope.value() instanceof byte[]) {
                BytesMessage bytesMessage = jmsSession.createBytesMessage();
                bytesMessage.writeBytes((byte[]) envelope.value());
                message = bytesMessage;
            } else if (envelope.value() instanceof String) {
                message = jmsSession.createTextMessage((String) envelope.value());
            } else {
                log.error("Unsupported record value type: {}", envelope.value().getClass());
                return;
            }

            producer.send(message);
            log.debug("Successfully sent message to JMS topic {}", kafkaTopic);
        } catch (JMSException e) {
            throw new RetriableException("Failed to send message to JMS topic " + kafkaTopic, e);
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
        String brokerURL = "amqp://artemis:5672"; // Default broker URL

        String[] parts = connectionString.split(";");

        for (String part : parts) {
            if (part.startsWith("Endpoint=sb://")) {
                String endpoint = part.substring("Endpoint=sb://".length());

                if (endpoint.endsWith("/")) {
                    endpoint = endpoint.substring(0, endpoint.length() - 1);
                }

                brokerURL = "amqps://" + endpoint;
            }

            if (part.startsWith("Endpoint=amqp://")) {
                String endpoint = part.substring("Endpoint=amqp://".length());

                if (endpoint.endsWith("/")) {
                    endpoint = endpoint.substring(0, endpoint.length() - 1);
                }

                brokerURL = "amqp://" + endpoint;
            }
        }

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
