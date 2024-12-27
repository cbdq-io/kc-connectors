Feature: Parse Azure Service Bus connection string

  Scenario Outline: Successfully parse a connection string
    Given a connection string "<connectionString>"
    When the connection string is parsed
    Then the brokerURL should be "<brokerURL>"
    And the userName should be "<userName>"
    And the password should be "<password>"

    Examples:
      | connectionString                                                                                                                | brokerURL                              | userName                  | password      |
      | Endpoint=amqp://artemis/;SharedAccessKeyName=artemis;SharedAccessKey=artemis                                                    | amqp://artemis                         | artemis                   | artemis       |
      | Endpoint=amqp://artemis;SharedAccessKeyName=artemis;SharedAccessKey=artemis                                                     | amqp://artemis                         | artemis                   | artemis       |
      | Endpoint=sb://example.servicebus.windows.net/;SharedAccessKeyName=keyName;SharedAccessKey=secretKey;                            | amqps://example.servicebus.windows.net | keyName                   | secretKey     |
      | Endpoint=sb://emulator;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true; | amqp://emulator:5672                   | RootManageSharedAccessKey | SAS_KEY_VALUE |

  Scenario Outline: Handle invalid connection string
    Given a connection string "<connectionString>"
    When the connection string is parsed
    Then an error should be thrown

    Examples:
      | connectionString                                                            |
      | InvalidConnectionString                                                     |
      | SharedAccessKeyName=artemis;SharedAccessKey=artemis                         |
      | Endpoint=amqp://artemis;SharedAccessKey=artemis                             |
      | Endpoint=sb://example.servicebus.windows.net/;SharedAccessKeyName=keyName;  |
      | Endpoint=foo://artemis/;SharedAccessKeyName=artemis;SharedAccessKey=artemis |
