package io.cbdq;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class AzureServiceBusSinkConnectorConfig extends AbstractConfig {

    public static final String CONNECTION_STRING_CONFIG = "azure.servicebus.connection.string";
    private static final String CONNECTION_STRING_DOC = "Azure Service Bus connection string.";

    public static final String LARGE_MESSAGE_THRESHOLD_BYTES_CONFIG = "large.message.threshold.bytes";
    private static final String LARGE_MESSAGE_THRESHOLD_BYTES_DOC = "Message size threshold in bytes above which a message is considered too large for batching (default: 524288 = 512KB).";

    public static final String RETRY_MAX_ATTEMPTS_CONFIG = "retry.max.attempts";
    private static final String RETRY_MAX_ATTEMPTS_DOC = "Maximum number of retry attempts.";

    public static final String RETRY_DELAY_MS_CONFIG = "retry.delay.ms";
    private static final String RETRY_DELAY_MS_DOC = "Base delay between retries in milliseconds.";

    public static final String RETRY_MAX_DELAY_MS_CONFIG = "retry.max.delay.ms";
    private static final String RETRY_MAX_DELAY_MS_DOC = "Maximum delay between retry attempts in milliseconds.";

    public static final String RETRY_TOTAL_TIMEOUT_MS_CONFIG = "retry.total.timeout.ms";
    private static final String RETRY_TOTAL_TIMEOUT_MS_DOC = "Maximum total timeout for all retry attempts in milliseconds.";

    public static final String RETRY_MODE_CONFIG = "retry.mode";
    private static final String RETRY_MODE_DOC = "Retry mode: 'exponential' or 'fixed'.";

    public static final String SET_KAFKA_PARTITION_AS_SESSION_ID_CONFIG = "set.kafka.partition.as.session.id";
    private static final String SET_KAFKA_PARTITION_AS_SESSION_ID_DOC = "Set Kafka partition as session ID.";

    public static final String TOPIC_RENAME_FORMAT_CONFIG = "topic.rename.format";
    private static final String TOPIC_RENAME_FORMAT_DOC = "Destination topic name format.";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(CONNECTION_STRING_CONFIG, ConfigDef.Type.PASSWORD, ConfigDef.Importance.HIGH, CONNECTION_STRING_DOC)
        .define(LARGE_MESSAGE_THRESHOLD_BYTES_CONFIG, ConfigDef.Type.INT, 524288, ConfigDef.Importance.MEDIUM, LARGE_MESSAGE_THRESHOLD_BYTES_DOC)
        .define(RETRY_MAX_ATTEMPTS_CONFIG, ConfigDef.Type.INT, 3, ConfigDef.Importance.MEDIUM, RETRY_MAX_ATTEMPTS_DOC)
        .define(RETRY_DELAY_MS_CONFIG, ConfigDef.Type.INT, 800, ConfigDef.Importance.MEDIUM, RETRY_DELAY_MS_DOC)
        .define(RETRY_MAX_DELAY_MS_CONFIG, ConfigDef.Type.INT, 8000, ConfigDef.Importance.MEDIUM, RETRY_MAX_DELAY_MS_DOC)
        .define(RETRY_TOTAL_TIMEOUT_MS_CONFIG, ConfigDef.Type.INT, 60000, ConfigDef.Importance.MEDIUM, RETRY_TOTAL_TIMEOUT_MS_DOC)
        .define(RETRY_MODE_CONFIG, ConfigDef.Type.STRING, "exponential", ConfigDef.Importance.MEDIUM, RETRY_MODE_DOC)
        .define(SET_KAFKA_PARTITION_AS_SESSION_ID_CONFIG, ConfigDef.Type.BOOLEAN, false, ConfigDef.Importance.MEDIUM, SET_KAFKA_PARTITION_AS_SESSION_ID_DOC)
        .define(TOPIC_RENAME_FORMAT_CONFIG, ConfigDef.Type.STRING, "${topic}", ConfigDef.Importance.HIGH, TOPIC_RENAME_FORMAT_DOC);

    public AzureServiceBusSinkConnectorConfig(Map<String, String> parsedConfig) {
        super(CONFIG_DEF, parsedConfig);
    }
}
