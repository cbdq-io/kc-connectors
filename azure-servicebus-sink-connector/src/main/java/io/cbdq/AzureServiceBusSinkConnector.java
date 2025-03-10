package io.cbdq;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;

import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.apache.kafka.common.config.ConfigDef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AzureServiceBusSinkConnector extends SinkConnector {

    private static final Logger log = LoggerFactory.getLogger(AzureServiceBusSinkConnector.class);
    private Map<String, String> configProperties;
    private HTTPServer prometheusServer;;

    @Override
    public void start(Map<String, String> props) {
        configProperties = props;
        // You can perform any initialization here if necessary
        log.info("Starting version {} of the connector.", VersionUtil.getVersion());
        String port = System.getenv("PROMETHEUS_PORT");

        try {
            if (port == null) {
                port = "9401"; // Default value
            }

            prometheusServer = HTTPServer.builder()
                .port(Integer.parseInt(port))
                .buildAndStart();
            log.info("Prometheus HTTPServer listening on port http://localhost:" + prometheusServer.getPort() + "/metrics");
        } catch (IOException e) {
            log.warn("Unable to open Prometheus HTTPServer on port {}.", port);
        }
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
        log.info("Stopping the connector.");
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
