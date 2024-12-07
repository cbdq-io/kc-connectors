# A Basic Sink Connector for Azure Service Bus

See <https://docs.confluent.io/platform/current/installation/configuration/connect/sink-connect-configs.html>
for the list of default configuration properties for sink connectors.

Please note the following:

- There is no changing of the topic name from Kafka to ASB. So there is a
  one-to-one mapping of a Kafka topic name to an ASB topic name.
- This connector only sinks to ASB topics, there is no functionality for queues.

## Custom Connector Properties

- `azure.servicebus.connection.string`: The connection string for the Azure
  Service Bus instance to connect and authenticate with.
- `prometheus.port`: The port for the Prometheus HTTP server to run on.
  Default is 9400.
- `retry.max.attempts`: The maximum number of attempts to connect to the ASB
  instance. Default is 3.
- `retry.wait.time.ms`: The time (in milliseconds) to wait between retries to
  connect to the ASB instance. Default is 1000.
- `topic.rename.format`: A format string for the topic name in the destination
  cluster, which may contain ${topic} as a placeholder for the originating
  topic name. For example, gb_${topic} for the topic widgets will map to the
  destination topic name gb_widgets. Default is "${topic}".
