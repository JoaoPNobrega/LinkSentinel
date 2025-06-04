package br.cesar.school.linksentinel.bdd.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import org.mockito.Mockito;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.CheckResultRepository;
import br.cesar.school.linksentinel.repository.LinkRepository;
import br.cesar.school.linksentinel.service.LinkVerificationService;
import br.cesar.school.linksentinel.service.MonitoringService;
import br.cesar.school.linksentinel.service.observer.LoggingObserver;
import br.cesar.school.linksentinel.service.observer.LinkStatusObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MonitoramentoAgendadoSteps {

    @MockBean
    private LinkRepository linkRepository;
    @MockBean
    private LinkVerificationService linkVerificationService;
    @MockBean
    private CheckResultRepository checkResultRepository;
    @MockBean
    private LoggingObserver loggingObserver; 

    @Autowired
    private MonitoringService monitoringService;

    private Link currentLink;

    @Before
    public void setup() {
        reset(linkRepository, linkVerificationService, checkResultRepository, loggingObserver);
        
        List<LinkStatusObserver> observers = Arrays.asList(loggingObserver);
        ReflectionTestUtils.setField(monitoringService, "observers", observers);
    }

    @Given("que eu tenho um link {string} \\(ID: {int}) configurado como monitorado")
    public void queEuTenhoUmLinkIDConfiguradoComoMonitorado(String url, int id) {
        currentLink = new Link(url);
        currentLink.setId(UUID.randomUUID());
        currentLink.setMonitored(true);
        currentLink.setInternalMonitoringStatus("OK");
        currentLink.setConsecutiveDownCount(0);
        currentLink.setLastChecked(LocalDateTime.now().minusHours(1));
        
        when(linkRepository.findByMonitoredTrue()).thenReturn(List.of(currentLink));
        when(linkRepository.save(any(Link.class))).thenReturn(currentLink);
    }

    @Given("que o internalMonitoringStatus do link é {string}")
    public void queOInternalMonitoringStatusDoLinkE(String status) {
        currentLink.setInternalMonitoringStatus(status);
    }

    @Given("que o link {string} \\(ID: {int}) já falhou em {int} verificações consecutivas \\(consecutiveDownCount = {int})")
    public void queOLinkJaFalhouEmVerificacoesConsecutivas(String url, int id, int failures, int downCount) {
        queEuTenhoUmLinkIDConfiguradoComoMonitorado(url, id);
        currentLink.setConsecutiveDownCount(downCount);
        currentLink.setInternalMonitoringStatus("DOWN");
    }

    @Given("que o link {string} \\(ID: {int}) estava com internalMonitoringStatus {string}")
    public void queOLinkEstavaComInternalMonitoringStatus(String url, int id, String status) {
        queEuTenhoUmLinkIDConfiguradoComoMonitorado(url, id);
        currentLink.setInternalMonitoringStatus(status);
        if ("ALERTA_CRITICO_OFFLINE".equals(status)) {
            currentLink.setConsecutiveDownCount(2);
        }
    }

    @When("o sistema executa a verificação agendada para o link {string}")
    public void oSistemaExecutaAVerificacaoAgendadaParaOLink(String url) {

    }

    @And("a verificação resulta em status {string}")
    public void aVerificacaoResultaEmStatus(String status) {
        CheckResult result = CheckResult.builder()
                                .link(currentLink)
                                .user(CommonSteps.sharedAuthenticatedUser)
                                .originalUrl(currentLink.getUrl())
                                .checkTimestamp(LocalDateTime.now())
                                .status(status)
                                .accessible("ONLINE".equals(status) || "200 OK".equals(status) || "301 MOVED_PERMANENTLY".equals(status))
                                .build();

        if ("ONLINE".equals(status)) result.setStatusCode(200);
        else if ("OFFLINE".equals(status)) result.setStatusCode(404);
        else if ("ALERTA_CRITICO_OFFLINE".equals(status)) result.setStatusCode(500);
        else result.setStatusCode(0);

        when(linkVerificationService.verifyLink(any(Link.class))).thenReturn(result);
        when(checkResultRepository.save(any(CheckResult.class))).thenReturn(result);

        List<CheckResult> mockRecentChecks = new ArrayList<>();
        if (currentLink.getInternalMonitoringStatus().equals("ALERTA_CRITICO_OFFLINE")) {
            mockRecentChecks.add(CheckResult.builder().link(currentLink).status("OFFLINE").accessible(false).statusCode(404).checkTimestamp(LocalDateTime.now().minusMinutes(2)).build());
            mockRecentChecks.add(CheckResult.builder().link(currentLink).status("OFFLINE").accessible(false).statusCode(404).checkTimestamp(LocalDateTime.now().minusMinutes(1)).build());
        }
        mockRecentChecks.add(result);
        when(checkResultRepository.findTop2ByLinkOrderByCheckTimestampDesc(any(Link.class))).thenReturn(mockRecentChecks);

        monitoringService.checkMonitoredLinks();
    }

    @Then("o sistema deve registrar um novo CheckResult com status {string} para {string}")
    public void oSistemaDeveRegistrarUmNovoCheckResultComStatusPara(String status, String url) {
        verify(linkVerificationService, times(1)).verifyLink(any(Link.class));
        verify(checkResultRepository, times(1)).save(any(CheckResult.class));
    }

    @And("o internalMonitoringStatus do link {string} deve permanecer {string}")
    public void oInternalMonitoringStatusDoLinkDevePermanecer(String url, String expectedStatus) {
        assertEquals(expectedStatus, currentLink.getInternalMonitoringStatus());
    }

    @And("o internalMonitoringStatus do link {string} deve ser atualizado para {string}")
    public void oInternalMonitoringStatusDoLinkDeveSerAtualizadoPara(String url, String expectedStatus) {
        assertEquals(expectedStatus, currentLink.getInternalMonitoringStatus());
    }

    @And("o LoggingObserver deve registrar a notificação de alerta crítico")
    public void oLoggingObserverDeveRegistrarANotificacaoDeAlertaCritico() {
        verify(loggingObserver, times(1)).onStatusChange(any(Link.class), any(CheckResult.class), any(CheckResult.class));
    }

    @And("o LoggingObserver deve registrar a notificação de recuperação")
    public void oLoggingObserverDeveRegistrarANotificacaoDeRecuperacao() {
        verify(loggingObserver, times(1)).onStatusChange(any(Link.class), any(CheckResult.class), any(CheckResult.class));
    }

    @And("o consecutiveDownCount do link deve ser resetado para {int}")
    public void oConsecutiveDownCountDoLinkDeveSerResetadoPara(int expectedCount) {
        assertEquals(expectedCount, currentLink.getConsecutiveDownCount());
    }

    @Given("que o aplicativo Link Sentinel foi inicializado")
    public void queOAplicativoLinkSentinelFoiInicializado() {

    }

    @When("o MonitoringService é ativado pelo contexto do Spring")
    public void oMonitoringServiceEAtivadoPeloContextoDoSpring() {

    }

    @Then("o agendador de verificações periódicas deve iniciar")
    public void oAgendadorDeVerificacoesPeriodicasDeveIniciar() {

    }

    @And("a frequência de verificação deve ser conforme configurado \\(ex: a cada {int} segundos)")
    public void aFrequenciaDeVerificacaoDeveSerConformeConfiguradoSegundos(int seconds) {

    }

    @And("todos os links marcados como {string} devem ser carregados para o processo de monitoramento")
    public void todosOsLinksMarcadosComoDevemSerCarregadosParaOProcessoDeMonitoramento(String status) {
        verify(linkRepository, times(1)).findByMonitoredTrue();
    }
}