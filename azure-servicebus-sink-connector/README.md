# A Basic Sink Connector for Azure Service Bus

See <https://docs.confluent.io/platform/current/installation/configuration/connect/source-connect-configs.html>
for the list of default configuration properties for connectors.

Please note the following:

- There is no changing of the topic name from Kafka to ASB.  So there is a one-to-one mapping of a Kafka topic name to an ASB topic name.
- This connector only sinks to ASB topics, there is no functionality for queues.

## Custom Connector Properties

- `azure.servicebus.connection.string`: The connection string for the Azure Service Bus instance to connect and authenticate with.
- `retry.max.attempts`: The maximum number of attempts to connect to the ASB instance.
- `retry.wait.time.ms`: The time (in milliseconds) to wait between retries to connect to the ASB instance.

## Using SonarQube Locally

In the `azure-servicebus-sink-connector` directory, run the following command:

```shell
docker compose up -d sonar
```

Go to http://localhost:9000/ and when SonarQube is started, log in with the
default credentials of admin/admin.  You will be forced to reset the
password (set it to something basic, this is just local stuff).

Subsequently you will be asked to create a project.  Create one "Manually"
and:

- Type `azure-servicebus-sink-connector` for Project display name.
- Type `azure-servicebus-sink-connector` for Project key.
- Type `develop` for the "main" branch.

Where the icon for producing the project manually was, a new icon will have
appeared for "Locally".  Click on that and generate a token, copy the token
and set it as a variable with:

```shell
SONAR_TOKEN='sqp_YOUR_TOKEN'
```

On the SonarCube console, press "Continue" and select the "Maven"
option.  It will give you an example command line, except you want
to run:

```shell
docker compose run --rm mvn -B clean verify sonar:sonar \
  -Dsonar.projectKey=azure-servicebus-sink-connector \
  -Dsonar.host.url=http://sonar:9000 \
  -Dsonar.login=${SONAR_TOKEN}
  ```

You will now be able to view the results of the report in the SonarQube
console.
