package br.cesar.school.linksentinel.service.observer;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoggingObserver implements LinkStatusObserver {

    @Override
    public void onStatusChange(Link link, CheckResult oldResult, CheckResult newResult) {
        StringBuilder logBuilder = new StringBuilder();

        logBuilder.append("\n╔═════════════════════════════════════════════════════════════════════════════╗\n");
        logBuilder.append("║                       ALERTA DE MUDANÇA DE STATUS                           ║\n");
        logBuilder.append("╠═════════════════════════════════════════════════════════════════════════════╣\n");
        logBuilder.append(String.format("║ URL: %s\n", link.getUrl()));
        logBuilder.append("║ --------------------------------------------------------------------------- ║\n");

        if (newResult == null) {
            logBuilder.append("║ Novo Resultado: NULO (Não foi possível obter o novo resultado da verificação).\n");
        } else {
            if (oldResult != null) {
                logBuilder.append(String.format("║ Status Anterior: HTTP %s | Acessível: %s | Em: %s\n",
                        oldResult.getStatusCode(),
                        oldResult.isAccessible() ? "SIM" : "NÃO",
                        oldResult.getCheckTimestamp()));
            } else {
                logBuilder.append("║ Status Anterior: Não há registro anterior ou é a primeira verificação.\n");
            }

            logBuilder.append(String.format("║ Novo Status    : HTTP %s | Acessível: %s | Em: %s\n",
                    newResult.getStatusCode(),
                    newResult.isAccessible() ? "SIM" : "NÃO",
                    newResult.getCheckTimestamp()));
            logBuilder.append("║ ---------------------------- DETALHES DA MUDANÇA -------------------------- ║\n");

            if (oldResult != null) {
                boolean accessibilityChanged = oldResult.isAccessible() != newResult.isAccessible();
                boolean statusCodeChanged = oldResult.getStatusCode() != newResult.getStatusCode();

                if (!accessibilityChanged && !statusCodeChanged) {
                     logBuilder.append("║ Nenhuma mudança detectada nos detalhes do CheckResult (HTTP, Acessibilidade).\n");
                } else {
                    if (accessibilityChanged) {
                        logBuilder.append(String.format("║ MUDANÇA: Acessibilidade mudou de %s para %s\n",
                                oldResult.isAccessible() ? "SIM" : "NÃO",
                                newResult.isAccessible() ? "SIM" : "NÃO"));
                    }
                    if (statusCodeChanged) {
                        logBuilder.append(String.format("║ MUDANÇA: Status HTTP mudou de %s para %s\n",
                                oldResult.getStatusCode(), newResult.getStatusCode()));
                    }
                }
            } else {
                logBuilder.append("║ MUDANÇA: Primeira verificação relevante ou link passou a ser monitorado.\n");
            }
        }
        
        logBuilder.append("║ --------------------------- STATUS INTERNO DO LINK -------------------------- ║\n");
        logBuilder.append(String.format("║ Status de Monitoramento do Link: %s\n", link.getInternalMonitoringStatus()));
        logBuilder.append(String.format("║ Contagem de Falhas Consecutivas: %d\n", link.getConsecutiveDownCount()));
        logBuilder.append("╚═════════════════════════════════════════════════════════════════════════════╝");

        String internalStatus = link.getInternalMonitoringStatus();
        if ("ALERTA_CRITICO_OFFLINE".equals(internalStatus) || "ERROR_MONITORING".equals(internalStatus)) {
            log.warn(logBuilder.toString());
        } else {
            log.info(logBuilder.toString());
        }
    }
}