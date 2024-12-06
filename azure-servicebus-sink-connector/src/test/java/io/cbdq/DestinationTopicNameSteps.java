package io.cbdq;

import static org.junit.Assert.*;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class DestinationTopicNameSteps {
    private DestinationTopicName renamer;
    private String sourceTopicName;

    @Before
    public void setUp() {
        // Initialization before each scenario
        sourceTopicName = null;
    }

    @After
    public void tearDown() {
        // Cleanup after each scenario
    }

    @Given("the Kafka topic name {string}")
    public void the_kafka_topic_name(String string) {
        sourceTopicName = string;
    }

    @When("the topic rename format is {string}")
    public void the_topic_rename_format_is(String string) {
        assertNotNull(string);
        renamer = new DestinationTopicName(string);
    }

    @Then("the expected desination topic name is {string}")
    public void the_expected_desination_topic_name_is(String expectedTopicName) {
        String actualTopicName = renamer.destination_topic_name(sourceTopicName);
        assertEquals(expectedTopicName, actualTopicName);
    }
}
