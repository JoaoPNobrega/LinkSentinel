package br.cesar.school.linksentinel.bdd;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(

    features = "file:///C:/Users/pedro/Desktop/demo/demo/docs/bdd",
    glue = "br.cesar.school.linksentinel.bdd.steps",
    plugin = {"pretty", "html:target/cucumber-reports.html", "json:target/cucumber-reports.json"},
    snippets = CucumberOptions.SnippetType.CAMELCASE,
    monochrome = true
)
@SpringBootTest
@ContextConfiguration(classes = br.cesar.school.linksentinel.App.class)
public class CucumberRunner {
}