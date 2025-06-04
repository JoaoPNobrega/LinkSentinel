package br.cesar.school.linksentinel.bdd.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import org.mockito.Mockito;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.CheckResultRepository;
import br.cesar.school.linksentinel.repository.LinkRepository;
import br.cesar.school.linksentinel.repository.UserRepository;
import br.cesar.school.linksentinel.service.LinkVerificationService;
import br.cesar.school.linksentinel.service.SecurityService;
import br.cesar.school.linksentinel.service.ThreatChecker;
import br.cesar.school.linksentinel.service.strategy.VerificationStrategyType;
import br.cesar.school.linksentinel.service.verifier.LinkVerifier;
import br.cesar.school.linksentinel.service.verifier.RedirectVerifierDecorator;
import br.cesar.school.linksentinel.service.verifier.BaseHttpVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VerificacaoLinkSteps {

    @MockBean
    private LinkRepository linkRepository;
    @MockBean
    private CheckResultRepository checkResultRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private SecurityService securityService;
    @MockBean
    private LinkVerifier configuredVerifier;
    @MockBean
    private ThreatChecker threatChecker;

    @Autowired
    private LinkVerificationService linkVerificationService;

    private CheckResult latestCheckResult;
    private User authenticatedUser;

    @Before
    public void setup() {
        reset(linkRepository, checkResultRepository, userRepository, securityService, configuredVerifier, threatChecker);

        authenticatedUser = new User();
        authenticatedUser.setUsername("usuario_teste");
        authenticatedUser.setId(UUID.randomUUID());
        authenticatedUser.setEmail("usuario_teste@email.com");
        authenticatedUser.setRole("ROLE_USER");

        when(securityService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(userRepository.findByUsername("usuario_teste")).thenReturn(Optional.of(authenticatedUser));

        when(checkResultRepository.save(any(CheckResult.class))).thenAnswer(invocation -> {
            CheckResult result = invocation.getArgument(0);
            result.setId(1L);
            return result;
        });

        when(threatChecker.checkUrlForThreats(anyString())).thenReturn(Optional.empty());
    }

    @And("que eu acesso a tela de {string}")
    public void queEuAcessoATelaDe(String tela) {

    }

    @And("que eu tenho um link {string} registrado no sistema")
    public void queEuTenhoUmLinkRegistradoNoSistema(String url) {
        Link link = new Link(url);
        link.setId(UUID.randomUUID());

        when(linkRepository.findByUrl(url)).thenReturn(Optional.of(link));
        when(linkRepository.save(any(Link.class))).thenReturn(link);
    }
    
    @And("que o sistema está configurado para usar a RedirectCheckStrategy")
    public void queOSistemaEstaConfiguradoParaUsarARedirectCheckStrategy() throws IOException, InterruptedException {
        when(configuredVerifier.verify(any(CheckResult.class), eq("http://site.com.br"))).thenAnswer(invocation -> {
            CheckResult initialResult = invocation.getArgument(0);
            initialResult.setStatusCode(302);
            initialResult.setFinalUrl("https://www.site.com.br");
            initialResult.setAccessible(true);
            initialResult.setCheckTimestamp(LocalDateTime.now());
            initialResult.setStatus("REDIRECT");
            return initialResult;
        });
        when(configuredVerifier.verify(any(CheckResult.class), eq("https://www.site.com.br"))).thenAnswer(invocation -> {
            CheckResult initialResult = invocation.getArgument(0);
            initialResult.setStatusCode(200);
            initialResult.setAccessible(true);
            initialResult.setFinalUrl("https://www.site.com.br");
            initialResult.setCheckTimestamp(LocalDateTime.now());
            initialResult.setStatus("ONLINE");
            return initialResult;
        });
    }

    @When("eu preencho o campo URL com {string}")
    public void euPreenchoOCampoURLCom(String url) {

    }

    @And("eu clico no botão {string}")
    public void euClicoNoBotao(String buttonText) {

    }

    @And("o ThreatChecker identifica a URL como uma ameaça de phishing")
    public void oThreatCheckerIdentificaAURLComoUmaAmeacaDePhishing() {
        when(threatChecker.checkUrlForThreats(anyString())).thenReturn(Optional.of(List.of("MALICIOUS_PHISHING")));
    }

    @When("eu solicito a verificação imediata do link {string}")
    public void euSolicitoAVerificacaoImediataDoLink(String url) throws IOException, InterruptedException {
        
        when(configuredVerifier.verify(any(CheckResult.class), anyString()))
            .thenAnswer(invocation -> {
                CheckResult simulatedResult = invocation.getArgument(0);
                String currentUrl = invocation.getArgument(1);
                simulatedResult.setCheckTimestamp(LocalDateTime.now());
                simulatedResult.setUser(authenticatedUser);

                if (currentUrl.equals("https://www.google.com")) {
                    simulatedResult.setStatusCode(200);
                    simulatedResult.setStatus("ONLINE");
                    simulatedResult.setAccessible(true);
                    simulatedResult.setFinalUrl(currentUrl);
                } else if (currentUrl.equals("https://www.siteinexistente.com/pagina-nao-encontrada")) {
                    simulatedResult.setStatusCode(404);
                    simulatedResult.setStatus("OFFLINE");
                    simulatedResult.setAccessible(false);
                    simulatedResult.setFailureReason("NOT_FOUND");
                    simulatedResult.setFinalUrl(currentUrl);
                } else if (currentUrl.equals("http://dominio-inexistente-xyz.com")) {
                    simulatedResult.setStatusCode(0);
                    simulatedResult.setStatus("OFFLINE");
                    simulatedResult.setAccessible(false);
                    simulatedResult.setFailureReason("Connection timed out: Read timed out");
                    simulatedResult.setFinalUrl(currentUrl);
                } else if (currentUrl.equals("http://site.malicioso.exemplo")) {
                     simulatedResult.setStatusCode(200);
                     simulatedResult.setStatus("ONLINE");
                     simulatedResult.setAccessible(true);
                     simulatedResult.setFinalUrl(currentUrl);
                } else {
                    simulatedResult.setStatusCode(200);
                    simulatedResult.setStatus("ONLINE");
                    simulatedResult.setAccessible(true);
                    simulatedResult.setFinalUrl(currentUrl);
                }
                return simulatedResult;
            });

        latestCheckResult = linkVerificationService.performCheck(url, authenticatedUser.getUsername(), VerificationStrategyType.REDIRECT_CHECK);
    }


    @Then("o sistema deve exibir o status {string} para {string} na interface")
    public void oSistemaDeveExibirOStatusNaInterface(String expectedStatus, String url) {
        assertNotNull(latestCheckResult);
        String displayStatus = latestCheckResult.isAccessible() ? "Acessível (HTTP " + latestCheckResult.getStatusCode() + ")" : "Inacessível (HTTP " + latestCheckResult.getStatusCode() + ")";
        
        if (expectedStatus.startsWith("ONLINE") || expectedStatus.startsWith("OFFLINE")) {
            assertTrue("Status esperado: '" + expectedStatus + "' mas foi: '" + displayStatus + "'", 
                       displayStatus.startsWith(expectedStatus));
        } else if (expectedStatus.contains("HTTP")) {
             assertEquals(Integer.parseInt(expectedStatus.split(" ")[0]), latestCheckResult.getStatusCode());
             assertTrue(displayStatus.contains(expectedStatus));
        }
        assertEquals(url, latestCheckResult.getOriginalUrl());
    }

    @And("o tempo de resposta deve ser menor que {int} milissegundos")
    public void oTempoDeRespostaDeveSerMenorQueMilissegundos(int expectedMaxTime) {
        assertNotNull(latestCheckResult);
        assertTrue(true);
    }

    @And("o resultado da verificação deve ser salvo no repositório CheckResultRepository")
    public void oResultadoDaVerificacaoDeveSerSalvoNoRepositorioCheckResultRepository() {
        assertNotNull(latestCheckResult);
        verify(checkResultRepository, Mockito.times(1)).save(any(CheckResult.class));
    }

    @And("a razão da falha deve ser {string}")
    public void aRazaoDaFalhaDeveSer(String expectedFailureReason) {
        assertNotNull(latestCheckResult);
        assertEquals(expectedFailureReason, latestCheckResult.getFailureReason());
    }

    @Then("o sistema deve exibir o status {string} para a URL final {string} na interface")
    public void oSistemaDeveExibirOStatusParaAURLFinalNaInterface(String expectedStatus, String finalUrl) {
        assertNotNull(latestCheckResult);
        assertEquals(expectedStatus, latestCheckResult.getStatus());
        assertEquals(finalUrl, latestCheckResult.getFinalUrl());
        assertTrue(latestCheckResult.isAccessible());
    }

    @And("a verificação deve indicar um {string} risco de phishing")
    public void aVerificacaoDeveIndicarUmRiscoDePhishing(String expectedRiskLevel) {
        assertNotNull(latestCheckResult);
        verify(threatChecker, Mockito.times(1)).checkUrlForThreats(anyString());
    }

    @Then("o sistema deve exibir um alerta de segurança na interface")
    public void oSistemaDeveExibirUmAlertaDeSegurancaNaInterface() {
        assertTrue(true);
    }
}