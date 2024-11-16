package io.cbdq;

public class ConnectionStringParser {

    private final String connectionString;

    public ConnectionStringParser(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getBrokerURL() {
        String brokerURL;
        String endpoint = null;
        String protocol = null;

        String[] parts = this.connectionString.split(";");

        for (String part : parts) {
            if (part.startsWith("Endpoint=sb://")) {
                endpoint = part.substring("Endpoint=sb://".length());
                protocol = "amqps://";
            } else if (part.startsWith("Endpoint=amqp://")) {
                endpoint = part.substring("Endpoint=amqp://".length());
                protocol = "amqp://";
            }
        }

        if (endpoint == null) {
            throw new IllegalArgumentException("No endpoint found in the Azure Service Bus connection string.");
        }

        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        brokerURL = protocol + endpoint;
        return brokerURL;
    }

    public String getUserName() {
        String username = "";
        String[] parts = connectionString.split(";");
        for (String part : parts) {
            if (part.startsWith("SharedAccessKeyName=")) {
                username = part.substring("SharedAccessKeyName=".length());
            }
        }
        return username;
    }

    public String getPassword() {
        String password = "";
        String[] parts = connectionString.split(";");
        for (String part : parts) {
            if (part.startsWith("SharedAccessKey=")) {
                password = part.substring("SharedAccessKey=".length());
            }
        }
        return password;
    }
}
