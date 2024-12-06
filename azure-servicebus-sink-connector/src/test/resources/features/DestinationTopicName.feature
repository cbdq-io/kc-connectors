Feature: Destination Topic Name
  Scenario Outline: Expected Topic Names from Differnt Patterns
    Given the Kafka topic name "<kafka_topic_name>"
    When the topic rename format is "<pattern>"
    Then the expected desination topic name is "<expected_topic_name>"

    Examples:
    | kafka_topic_name | pattern          | expected_topic_name |
    | topic1           | ${topic}         | topic1              |
    | topic2           | foo-${topic}     | foo-topic2          |
    | topic3           | ${topic}-bar     | topic3-bar          |
    | topic4           | foo-${topic}-bar | foo-topic4-bar      |
