package io.cbdq;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class AzureServiceBusSinkConnectorConfig extends AbstractConfig {

    public static final String CONNECTION_STRING_CONFIG = "azure.servicebus.connection.string";
    private static final String CONNECTION_STRING_DOC = "Azure Service Bus connection string.";

    public static final String RETRY_MAX_ATTEMPTS_CONFIG = "retry.max.attempts";
    private static final String RETRY_MAX_ATTEMPTS_DOC = "Maximum number of retry attempts.";

    public static final String RETRY_WAIT_TIME_MS_CONFIG = "retry.wait.time.ms";
    private static final String RETRY_WAIT_TIME_MS_DOC = "Wait time between retries in milliseconds.";

    public static final String SET_KAFKA_PARTITION_AS_SESSION_ID_CONFIG = "set.kafka.partition.as.session.id";
    private static final String SET_KAFKA_PARTITION_AS_SESSION_ID_DOC = "Should the Kafka partition of a message be set as the session ID.  Defaults is false.";

    public static final String TOPIC_RENAME_FORMAT_CONFIG = "topic.rename.format";
    private static final String TOPIC_RENAME_FORMAT_DOC = "A format string for the topic name in the destination cluster, which may contain ${topic} as a placeholder for the originating topic name.";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(CONNECTION_STRING_CONFIG, ConfigDef.Type.PASSWORD, ConfigDef.Importance.HIGH, CONNECTION_STRING_DOC)
            .define(RETRY_MAX_ATTEMPTS_CONFIG, ConfigDef.Type.INT, 3, ConfigDef.Importance.MEDIUM, RETRY_MAX_ATTEMPTS_DOC)
            .define(RETRY_WAIT_TIME_MS_CONFIG, ConfigDef.Type.INT, 1000, ConfigDef.Importance.MEDIUM, RETRY_WAIT_TIME_MS_DOC)
            .define(SET_KAFKA_PARTITION_AS_SESSION_ID_CONFIG, ConfigDef.Type.BOOLEAN, false, ConfigDef.Importance.MEDIUM, SET_KAFKA_PARTITION_AS_SESSION_ID_DOC)
            .define(TOPIC_RENAME_FORMAT_CONFIG, ConfigDef.Type.STRING, "${topic}", ConfigDef.Importance.HIGH, TOPIC_RENAME_FORMAT_DOC);

    public AzureServiceBusSinkConnectorConfig(Map<String, String> parsedConfig) {
        super(CONFIG_DEF, parsedConfig);
    }
}
