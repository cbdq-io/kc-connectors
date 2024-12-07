package io.cbdq;

public class AzureServiceBusSinkException extends RuntimeException {
    public AzureServiceBusSinkException(String message) {
        super(message);
    }

    public AzureServiceBusSinkException(String message, Throwable cause) {
        super(message, cause);
    }
}
