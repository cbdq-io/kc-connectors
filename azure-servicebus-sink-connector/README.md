# A Basic Sink Connector for Azure Service Bus

See <https://docs.confluent.io/platform/current/installation/configuration/connect/sink-connect-configs.html>
for the list of default configuration properties for sink connectors.

Please note the following:

- This connector only sinks to ASB topics, there is no functionality for queues.

## Custom Connector Properties

- `azure.servicebus.connection.string`: The connection string for the Azure
  Service Bus instance to connect and authenticate with.
- `large.message.threshold.bytes`: Message size threshold in bytes above
  which a message is considered too large for batching (default: 524288 =
  512KB).
- `retry.max.attempts`: The maximum number of attempts to connect to the ASB
  instance. Default is 3.
- `retry.wait.time.ms`: The time (in milliseconds) to wait between retries to
  connect to the ASB instance. Default is 1000.
- `topic.rename.format`: A format string for the topic name in the destination
  cluster, which may contain `${topic}` as a placeholder for the originating
  topic name. For example, `gb_${topic}` for the topic widgets will map to the
  destination topic name `gb_widgets`. Default is `"${topic}"`.
