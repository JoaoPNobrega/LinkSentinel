package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.repository.CheckResultRepository;
import br.cesar.school.linksentinel.repository.LinkRepository;
import br.cesar.school.linksentinel.service.observer.LinkStatusObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final LinkRepository linkRepository;
    private final LinkVerificationService linkVerificationService;
    private final CheckResultRepository checkResultRepository;
    private final List<LinkStatusObserver> observers;

    @Scheduled(fixedRate = 60000, initialDelay = 15000)
    @Transactional
    public void checkMonitoredLinks() {
        log.info("==== Iniciando verificação agendada de links monitorados ====");
        List<Link> monitoredLinks = linkRepository.findByMonitoredTrue();

        if (monitoredLinks.isEmpty()) {
            log.info("Nenhum link marcado para monitoramento no momento.");
        } else {
            log.info("Encontrados {} links para monitorar.", monitoredLinks.size());
            for (Link link : monitoredLinks) {
                log.info("Monitorando link: {}", link.getUrl());
                CheckResult currentCheckResult = null;
                try {
                    currentCheckResult = linkVerificationService.verifyLink(link);

                    if (currentCheckResult.getStatusCode() >= 200 && currentCheckResult.getStatusCode() < 400) {
                        link.setConsecutiveDownCount(0);
                        link.setInternalMonitoringStatus("UP");
                    } else {
                        link.setConsecutiveDownCount(link.getConsecutiveDownCount() + 1);
                        if (link.getConsecutiveDownCount() >= 2) {
                            link.setInternalMonitoringStatus("ALERTA_CRITICO_OFFLINE");
                            log.warn("ALERTA CRITICO OFFLINE para o link {}: {} falhas consecutivas.", link.getUrl(), link.getConsecutiveDownCount());
                        } else {
                            link.setInternalMonitoringStatus("DOWN");
                        }
                    }
                    linkRepository.save(link);
                    log.info("Link {} atualizado. Status interno: {}, Falhas consecutivas: {}", link.getUrl(), link.getInternalMonitoringStatus(), link.getConsecutiveDownCount());

                    List<CheckResult> lastTwoResults = checkResultRepository.findTop2ByLinkOrderByCheckTimestampDesc(link);
                    CheckResult previousResultInDb = null;

                    if (lastTwoResults.size() > 1) {
                        if (lastTwoResults.get(0).getId().equals(currentCheckResult.getId())) {
                            previousResultInDb = lastTwoResults.get(1);
                        } else {
                             log.warn("O resultado mais recente no banco para o link {} (ID: {}) não corresponde ao resultado da verificação atual (ID: {}). O resultado anterior pode não ser o esperado para comparação.",
                                link.getUrl(), lastTwoResults.get(0).getId(), currentCheckResult.getId());
                             previousResultInDb = lastTwoResults.get(0);
                        }
                    } else if (lastTwoResults.size() == 1 && !lastTwoResults.get(0).getId().equals(currentCheckResult.getId())) {
                        log.warn("Apenas um resultado no banco para o link {} e ele não é o atual. Considerado como sem histórico anterior para comparação.", link.getUrl());
                    }


                    compareAndNotify(link, previousResultInDb, currentCheckResult);

                } catch (Exception e) {
                    log.error("Erro ao verificar ou processar link monitorado {}: {}", link.getUrl(), e.getMessage(), e);
                    try {
                        link.setInternalMonitoringStatus("ERROR_MONITORING");
                        linkRepository.save(link);
                    } catch (Exception ex) {
                        log.error("Erro ao tentar salvar status de erro para o link {}: {}", link.getUrl(), ex.getMessage(), ex);
                    }
                }
            }
        }
        log.info("==== Verificação agendada de links monitorados concluída ====");
    }

    private void compareAndNotify(Link link, CheckResult oldResult, CheckResult newResult) {
        if (newResult == null) {
            log.warn("Tentativa de comparar com um novo resultado nulo para o link: {}", link.getUrl());
            return;
        }

        boolean significantChange = false;
        if (oldResult == null) {
            significantChange = true;
            log.info("Primeira verificação monitorada (ou sem histórico comparável) para {}. Status atual: HTTP {}, Acessível: {}. Status Interno Link: {}",
                    link.getUrl(), newResult.getStatusCode(), newResult.isAccessible(), link.getInternalMonitoringStatus());
        } else {
            if (oldResult.isAccessible() != newResult.isAccessible()) {
                significantChange = true;
                log.info("Mudança na acessibilidade detectada para {}: de {} para {}. Status Interno Link: {}",
                        link.getUrl(), oldResult.isAccessible(), newResult.isAccessible(), link.getInternalMonitoringStatus());
            }
            if (oldResult.getStatusCode() != newResult.getStatusCode()) {
                significantChange = true;
                log.info("Mudança no status HTTP detectada para {}: de {} para {}. Status Interno Link: {}",
                        link.getUrl(), oldResult.getStatusCode(), newResult.getStatusCode(), link.getInternalMonitoringStatus());
            }
            if (!link.getInternalMonitoringStatus().equals(oldResult.getLink() != null ? oldResult.getLink().getInternalMonitoringStatus() : "UNKNOWN") && !"ERROR_MONITORING".equals(link.getInternalMonitoringStatus())) {
                 // Esta comparação do internalMonitoringStatus pode ser complexa se oldResult.getLink() não estiver atualizado
                 // ou se o estado de 'link' no oldResult for diferente do estado atual de 'link' (o objeto).
                 // A notificação baseada no estado atual do 'link' já persistido é mais confiável.
                 // Vamos considerar a mudança no link.getInternalMonitoringStatus() como significativa
                 // para garantir que os observadores sejam notificados do novo status de alerta.
                 // No entanto, a lógica de 'significantChange' atual foca nos CheckResults.
                 // O link atualizado já é passado para notifyObservers.
            }
        }

        if (significantChange || "ALERTA_CRITICO_OFFLINE".equals(link.getInternalMonitoringStatus()) || "ERROR_MONITORING".equals(link.getInternalMonitoringStatus())) {
           if(!significantChange){
                log.info("Notificando devido a status crítico ou erro de monitoramento para {}. Status: {}", link.getUrl(), link.getInternalMonitoringStatus());
           }
            notifyObservers(link, oldResult, newResult);
        } else {
            log.info("Nenhuma mudança significativa (baseada em CheckResult) ou alerta crítico novo detectado para o link {} para acionar notificação primária. Status Interno: {}", link.getUrl(), link.getInternalMonitoringStatus());
        }
    }

    private void notifyObservers(Link link, CheckResult oldResult, CheckResult newResult) {
        log.info("Notificando {} observadores sobre o link {}", observers.size(), link.getUrl());
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