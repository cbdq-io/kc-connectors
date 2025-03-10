package io.cbdq;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

public class PrometheusMetrics {

    private static PrometheusMetrics INSTANCE;
    private final Counter messageCounter;

    private PrometheusMetrics(String connectorName) {
        // JVM Metrics (registers once)
        JvmMetrics.builder().register();

        // Keep your original metric name pattern
        messageCounter = Counter.builder()
                .name(connectorName + "_message_count_total")  // Uses the original naming pattern
                .help("The number of messages processed.")
                .register();
    }

    public static synchronized PrometheusMetrics getInstance(String connectorName) {
        if (INSTANCE == null) {
            INSTANCE = new PrometheusMetrics(connectorName);
        }
        return INSTANCE;
    }

    public void incrementMessageCounter() {
        messageCounter.inc();
    }
}
