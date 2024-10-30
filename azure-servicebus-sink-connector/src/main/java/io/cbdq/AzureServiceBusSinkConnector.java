package io.cbdq;

import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.apache.kafka.common.config.ConfigDef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AzureServiceBusSinkConnector extends SinkConnector {

    private static final Logger log = LoggerFactory.getLogger(AzureServiceBusSinkTask.class);
    private Map<String, String> configProperties;

    @Override
    public void start(Map<String, String> props) {
        configProperties = props;
        // You can perform any initialization here if necessary
        log.debug("k2sbus - Starting the connector {}", props);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return AzureServiceBusSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> configs = new ArrayList<>();

        for (int i = 0; i < maxTasks; i++) {
            configs.add(configProperties);
        }

        return configs;
    }

    @Override
    public void stop() {
        // Perform any cleanup if necessary
        log.info("k2sbus - Stopping the connector.");
    }

    @Override
    public ConfigDef config() {
        return AzureServiceBusSinkConnectorConfig.CONFIG_DEF;
    }

    @Override
    public String version() {
        return VersionUtil.getVersion();
    }
}
