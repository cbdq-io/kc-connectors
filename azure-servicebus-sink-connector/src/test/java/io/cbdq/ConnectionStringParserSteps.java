package io.cbdq;

import static org.junit.Assert.*;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class ConnectionStringParserSteps {

    private ConnectionStringParser parser;
    private String connectionString;
    private Exception exception;

    @Before
    public void setUp() {
        // Initialization before each scenario
        connectionString = null;
        parser = null;
        exception = null;
    }

    @After
    public void tearDown() {
        // Cleanup after each scenario
    }

    @Given("a connection string {string}")
    public void aConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @When("the connection string is parsed")
    public void theConnectionStringIsParsed() {
        try {
            parser = new ConnectionStringParser(connectionString);
            parser.getBrokerURL();
            parser.getUserName();
            parser.getPassword();
        } catch (Exception e) {
            exception = e;
        }
    }

    @Then("the brokerURL should be {string}")
    public void theBrokerUrlShouldBe(String expectedBrokerURL) {
        assertEquals(expectedBrokerURL, parser.getBrokerURL());
    }

    @Then("the userName should be {string}")
    public void theUserNameShouldBe(String expectedUserName) {
        assertEquals(expectedUserName, parser.getUserName());
    }

    @Then("the password should be {string}")
    public void thePasswordShouldBe(String expectedPassword) {
        assertNotNull("Parser should not be null", parser);
        assertEquals(expectedPassword, parser.getPassword());
    }

    @Then("an error should be thrown")
    public void anErrorShouldBeThrown() {
        assertNotNull("Exception should be thrown", exception);
    }
}
