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

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(CONNECTION_STRING_CONFIG, ConfigDef.Type.PASSWORD, ConfigDef.Importance.HIGH, CONNECTION_STRING_DOC)
            .define(RETRY_MAX_ATTEMPTS_CONFIG, ConfigDef.Type.INT, 3, ConfigDef.Importance.MEDIUM, RETRY_MAX_ATTEMPTS_DOC)
            .define(RETRY_WAIT_TIME_MS_CONFIG, ConfigDef.Type.INT, 1000, ConfigDef.Importance.MEDIUM, RETRY_WAIT_TIME_MS_DOC);

    public AzureServiceBusSinkConnectorConfig(Map<String, String> parsedConfig) {
        super(CONFIG_DEF, parsedConfig);
    }
}
