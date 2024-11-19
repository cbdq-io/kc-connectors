package io.cbdq;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",  // Path to feature files
    glue = "io.cbdq",                // Package with step definitions
    plugin = {"pretty", "html:target/cucumber-reports.html"},  // Plugins for reporting
    monochrome = true                          // For better console output
)
public class RunCucumberTest {
}
