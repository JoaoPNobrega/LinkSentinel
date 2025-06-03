package br.cesar.school.linksentinel.bdd;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@RunWith(Cucumber.class)
@CucumberOptions(
    // ATENÇÃO: Use o caminho ABSOLUTO que você confirmou no seu sistema.
    // As barras devem ser normais (/) e o prefixo "file:///" é opcional, mas seguro.
    features = "file:///C:/Users/pedro/Desktop/demo/demo/docs/bdd", // <-- CORRIGIDO AQUI!
    glue = "br.cesar.school.linksentinel.bdd.steps",
    plugin = {"pretty", "html:target/cucumber-reports.html", "json:target/cucumber-reports.json"},
    snippets = CucumberOptions.SnippetType.CAMELCASE,
    monochrome = true
)
@SpringBootTest
@ContextConfiguration(classes = br.cesar.school.linksentinel.App.class)
public class CucumberRunner {
    // Esta classe continua sem métodos.
}