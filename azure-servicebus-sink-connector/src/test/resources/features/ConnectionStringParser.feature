Feature: Parse Azure Service Bus connection string

  Scenario Outline: Successfully parse a connection string
    Given a connection string "<connectionString>"
    When the connection string is parsed
    Then the brokerURL should be "<brokerURL>"
    And the userName should be "<userName>"
    And the password should be "<password>"

    Examples:
      | connectionString                                                                                     | brokerURL                              | userName | password  |
      | Endpoint=amqp://artemis/;SharedAccessKeyName=artemis;SharedAccessKey=artemis                         | amqp://artemis                         | artemis  | artemis   |
      | Endpoint=amqp://artemis;SharedAccessKeyName=artemis;SharedAccessKey=artemis                          | amqp://artemis                         | artemis  | artemis   |
      | Endpoint=sb://example.servicebus.windows.net/;SharedAccessKeyName=keyName;SharedAccessKey=secretKey; | amqps://example.servicebus.windows.net | keyName  | secretKey |

  Scenario Outline: Handle invalid connection string
    Given a connection string "<connectionString>"
    When the connection string is parsed
    Then an error should be thrown

    Examples:
      | connectionString             |
      | InvalidConnectionString      |
      | AnotherInvalidConnection     |