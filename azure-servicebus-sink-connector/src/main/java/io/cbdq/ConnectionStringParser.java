package io.cbdq;

public class ConnectionStringParser {

    private final String connectionString;
    private String endpoint;
    private String username;
    private String password;
    private boolean useDevelopmentEmulator;

    public ConnectionStringParser(String connectionString) {
        this.connectionString = connectionString;
        this.useDevelopmentEmulator = Boolean.parseBoolean(getValueFromConnectionString("UseDevelopmentEmulator", "false"));
        this.endpoint = getValueFromConnectionString("Endpoint");
        this.username = getValueFromConnectionString("SharedAccessKeyName");
        this.password = getValueFromConnectionString("SharedAccessKey");
    }

    public String getValueFromConnectionString(String keyName, String... defaultValue) {
        String response = null;
        String[] parts = this.connectionString.split(";");
        String prefix = keyName + "=";

        for (String part : parts) {
            part = part.strip();

            if (part.length() == 0) {
                continue;
            }


            if (part.startsWith(prefix)) {
                response = part;
            }
        }

        if (response == null) {
            if (defaultValue.length > 0) {
                return defaultValue[0];
            }

            String message = String.format("No %s found in the Azure Service Bus connection string.", keyName);
            throw new IllegalArgumentException(message);
        }

        return response.substring(prefix.length());
    }

    public String getBrokerURL() {
        String brokerURL;
        String hostname = null;
        String protocol = null;
        String nonSslAMQP = "amqp://";

        if (this.endpoint.startsWith("sb://")) {
            hostname = this.endpoint.substring("sb://".length());

            if (this.useDevelopmentEmulator) {
                protocol = nonSslAMQP;
            } else {
                protocol = "amqps://";
            }
        } else if (this.endpoint.startsWith(nonSslAMQP)) {
            hostname = this.endpoint.substring(nonSslAMQP.length());
            protocol = nonSslAMQP;
        } else {
            String message = String.format("Unrecognised protocol for endpoint (%s).", this.endpoint);
            throw new IllegalArgumentException(message);
        }

        if (hostname.endsWith("/")) {
            hostname = hostname.substring(0, hostname.length() - 1);
        }

        brokerURL = protocol + hostname;

        if (this.useDevelopmentEmulator) {
            brokerURL = brokerURL + ":5672";
        }

        return brokerURL;
    }

    public String getUserName() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }
}
