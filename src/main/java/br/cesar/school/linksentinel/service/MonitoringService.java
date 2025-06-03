package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.repository.CheckResultRepository;
import br.cesar.school.linksentinel.repository.LinkRepository;
import br.cesar.school.linksentinel.service.observer.LinkStatusObserver;
import br.cesar.school.linksentinel.service.strategy.VerificationStrategyType; // Importação está correta
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
                CheckResult newResultFromVerification = null; // Renomeado para clareza
                try {
                    newResultFromVerification = linkVerificationService.performCheck(
                            link.getUrl(),
                            MONITORING_USERNAME,
                            VerificationStrategyType.REDIRECT_CHECK
                    );
                    // Usar o resultado retornado pela verificação que foi salvo no BD
                    log.info("Link {} verificado com sucesso pelo monitor. Status HTTP: {}", link.getUrl(), newResultFromVerification.getStatusCode()); // CORRIGIDO

                    // Buscamos os dois últimos resultados do banco DEPOIS que o novo resultado da verificação foi salvo.
                    // O 'newResultFromVerification' já é o mais recente e está no banco.
                    List<CheckResult> lastTwoResults = checkResultRepository.findTop2ByLinkOrderByCheckTimestampDesc(link);

                    CheckResult currentResultInDb = null;
                    CheckResult previousResultInDb = null;

                    if (!lastTwoResults.isEmpty()) {
                        currentResultInDb = lastTwoResults.get(0); // Este é o newResultFromVerification
                    }
                    if (lastTwoResults.size() > 1) {
                        previousResultInDb = lastTwoResults.get(1); // Este é o resultado anterior a newResultFromVerification
                    }
                    
                    // Passar o resultado anterior (previousResultInDb) e o atual (currentResultInDb) para comparação.
                    compareAndNotify(link, previousResultInDb, currentResultInDb);

                } catch (Exception e) {
                    log.error("Erro ao verificar ou processar link monitorado {}: {}", link.getUrl(), e.getMessage(), e);
                }
            }
        }
        log.info("==== Verificação agendada de links monitorados concluída ====");
    }

    private void compareAndNotify(Link link, CheckResult oldResult, CheckResult newResult) {
        if (newResult == null) { // newResult é o resultado atual da verificação
            log.warn("Tentativa de comparar com um novo resultado nulo para o link: {}", link.getUrl());
            return;
        }

        boolean significantChange = false;
        if (oldResult == null) { // Não há resultado anterior no banco para comparar
            significantChange = true;
            log.info("Primeira verificação monitorada (ou sem histórico comparável) para {}. Status atual: HTTP {}, Acessível: {}",
                    link.getUrl(), newResult.getStatusCode(), newResult.isAccessible()); // CORRIGIDO
        } else {
            // Compara acessibilidade
            if (oldResult.isAccessible() != newResult.isAccessible()) { // CORRIGIDO
                significantChange = true;
                log.info("Mudança na acessibilidade detectada para {}: de {} para {}",
                        link.getUrl(), oldResult.isAccessible(), newResult.isAccessible()); // CORRIGIDO
            }
            // Compara status code
            if (oldResult.getStatusCode() != newResult.getStatusCode()) { // CORRIGIDO
                significantChange = true;
                log.info("Mudança no status HTTP detectada para {}: de {} para {}",
                        link.getUrl(), oldResult.getStatusCode(), newResult.getStatusCode()); // CORRIGIDO
            }
        }

        if (significantChange) {
            notifyObservers(link, oldResult, newResult); // oldResult pode ser null aqui
        } else {
            log.info("Nenhuma mudança significativa detectada para o link {}", link.getUrl());
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