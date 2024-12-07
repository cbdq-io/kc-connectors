package io.cbdq;

import static org.junit.Assert.*;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class DestinationTopicNameSteps {
    private TopicRenameFormat renamer;
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
    public void theKafkaTopicName(String string) {
        sourceTopicName = string;
    }

    @When("the topic rename format is {string}")
    public void theTopicRenameFormatIs(String string) {
        assertNotNull(string);
        renamer = new TopicRenameFormat(string);
    }

    @Then("the expected desination topic name is {string}")
    public void theEpectedDestinattionTopicNameIs(String expectedTopicName) {
        String actualTopicName = renamer.rename(sourceTopicName);
        assertEquals(expectedTopicName, actualTopicName);
    }
}
