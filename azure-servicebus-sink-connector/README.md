# A Basic Sink Connector for Azure Service Bus

See <https://docs.confluent.io/platform/current/installation/configuration/connect/source-connect-configs.html>
for the list of default configuration properties for connectors.

Please note the following:

- There is no changing of the topic name from Kafka to ASB. So there is a
  one-to-one mapping of a Kafka topic name to an ASB topic name.
- This connector only sinks to ASB topics, there is no functionality for queues.

## Custom Connector Properties

- `azure.servicebus.connection.string`: The connection string for the Azure
  Service Bus instance to connect and authenticate with.
- `retry.max.attempts`: The maximum number of attempts to connect to the ASB
  instance.
- `retry.wait.time.ms`: The time (in milliseconds) to wait between retries to
  connect to the ASB instance.
