package io.github.mrergos.gymcrm.component.runner;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/component")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "io.github.mrergos.gymcrm.component")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, summary, html:target/cucumber-reports/gym-crm-component.html")
public class RunCucumberComponentTests {
}