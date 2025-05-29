package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.repository.CheckResultRepository;
import br.cesar.school.linksentinel.repository.LinkRepository;
import br.cesar.school.linksentinel.service.observer.LinkStatusObserver;
import br.cesar.school.linksentinel.service.strategy.VerificationStrategyType; // <-- ADICIONAR ESTA IMPORTAÇÃO
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final LinkRepository linkRepository;
    private final LinkVerificationService linkVerificationService;
    private final CheckResultRepository checkResultRepository;

    private final List<LinkStatusObserver> observers;

    private static final String MONITORING_USERNAME = "system-monitor";

    @Scheduled(fixedRate = 60000, initialDelay = 15000)
    public void checkMonitoredLinks() {
        log.info("==== Iniciando verificação agendada de links monitorados ====");
        List<Link> monitoredLinks = linkRepository.findByMonitoredTrue();

        if (monitoredLinks.isEmpty()) {
            log.info("Nenhum link marcado para monitoramento no momento.");
        } else {
            log.info("Encontrados {} links para monitorar.", monitoredLinks.size());
            for (Link link : monitoredLinks) {
                log.info("Monitorando link: {}", link.getUrl());
                CheckResult newResult = null;
                try {
                    // *** ALTERAÇÃO AQUI: Passando o tipo de estratégia ***
                    newResult = linkVerificationService.performCheck(
                            link.getUrl(),
                            MONITORING_USERNAME,
                            VerificationStrategyType.REDIRECT_CHECK // Usando a estratégia que verifica redirects
                    );
                    log.info("Link {} verificado com sucesso pelo monitor. Status HTTP: {}", link.getUrl(), newResult.getHttpStatusCode());

                    List<CheckResult> lastTwoResults = checkResultRepository.findTop2ByLinkOrderByCheckTimestampDesc(link);

                    CheckResult currentResultInDb = null;
                    CheckResult previousResult = null;

                    if (!lastTwoResults.isEmpty()) {
                        currentResultInDb = lastTwoResults.get(0);
                    }
                    if (lastTwoResults.size() > 1) {
                        previousResult = lastTwoResults.get(1);
                    }

                    compareAndNotify(link, previousResult, currentResultInDb);

                } catch (Exception e) {
                    log.error("Erro ao verificar ou processar link monitorado {}: {}", link.getUrl(), e.getMessage(), e);
                }
            }
        }
        log.info("==== Verificação agendada de links monitorados concluída ====");
    }

    private void compareAndNotify(Link link, CheckResult oldResult, CheckResult newResult) {
        if (newResult == null) return;

        boolean significantChange = false;
        if (oldResult == null) {
            significantChange = true;
            log.info("Primeira verificação monitorada para {}. Status atual: HTTP {}, Acessível: {}",
                    link.getUrl(), newResult.getHttpStatusCode(), newResult.getReachable());
        } else {
            if (!Objects.equals(oldResult.getReachable(), newResult.getReachable())) {
                significantChange = true;
                log.info("Mudança na acessibilidade detectada para {}: de {} para {}",
                        link.getUrl(), oldResult.getReachable(), newResult.getReachable());
            }
            if (!Objects.equals(oldResult.getHttpStatusCode(), newResult.getHttpStatusCode())) {
                significantChange = true;
                log.info("Mudança no status HTTP detectada para {}: de {} para {}",
                        link.getUrl(), oldResult.getHttpStatusCode(), newResult.getHttpStatusCode());
            }
        }

        if (significantChange) {
            notifyObservers(link, oldResult, newResult);
        }
    }

    private void notifyObservers(Link link, CheckResult oldResult, CheckResult newResult) {
        log.info("Notificando {} observadores sobre mudança no link {}", observers.size(), link.getUrl());
        for (LinkStatusObserver observer : observers) {
            try {
                observer.onStatusChange(link, oldResult, newResult);
            } catch (Exception e) {
                log.error("Erro ao notificar observador {} sobre o link {}: {}",
                        observer.getClass().getSimpleName(), link.getUrl(), e.getMessage(), e);
            }
        }
    }
}