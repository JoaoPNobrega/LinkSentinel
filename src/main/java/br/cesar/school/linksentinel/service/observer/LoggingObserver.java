package br.cesar.school.linksentinel.service.observer;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component // Marca como um componente Spring para que possa ser injetado/descoberto
@Slf4j
public class LoggingObserver implements LinkStatusObserver {

    @Override
    public void onStatusChange(Link link, CheckResult oldResult, CheckResult newResult) {
        log.info("╔═════════════════════════════════════════════════════════════════════════════╗");
        log.info("║          ALERTA DE MUDANÇA DE STATUS PARA O LINK MONITORADO                 ║");
        log.info("╠═════════════════════════════════════════════════════════════════════════════╣");
        log.info("║ URL: {}", link.getUrl());
        log.info("║ --------------------------------------------------------------------------- ║");

        if (oldResult != null) {
            log.info("║ Status Anterior: HTTP {} | Acessível: {} | Em: {}",
                    oldResult.getHttpStatusCode(),
                    Boolean.TRUE.equals(oldResult.getReachable()) ? "SIM" : "NÃO",
                    oldResult.getCheckTimestamp());
        } else {
            log.info("║ Status Anterior: Não há registro anterior significativo ou é a primeira verificação após monitorar.");
        }

        log.info("║ Novo Status    : HTTP {} | Acessível: {} | Em: {}",
                newResult.getHttpStatusCode(),
                Boolean.TRUE.equals(newResult.getReachable()) ? "SIM" : "NÃO",
                newResult.getCheckTimestamp());
        log.info("║ --------------------------------------------------------------------------- ║");

        // Detalhando a mudança
        if (oldResult != null) {
            if (!oldResult.getReachable().equals(newResult.getReachable())) {
                log.info("║ MUDANÇA: Acessibilidade mudou de {} para {}",
                        Boolean.TRUE.equals(oldResult.getReachable()) ? "SIM" : "NÃO",
                        Boolean.TRUE.equals(newResult.getReachable()) ? "SIM" : "NÃO");
            }
            if (oldResult.getHttpStatusCode() != null && newResult.getHttpStatusCode() != null &&
                !oldResult.getHttpStatusCode().equals(newResult.getHttpStatusCode())) {
                log.info("║ MUDANÇA: Status HTTP mudou de {} para {}",
                        oldResult.getHttpStatusCode(), newResult.getHttpStatusCode());
            }
        } else {
            log.info("║ MUDANÇA: Primeira verificação relevante ou link passou a ser monitorado.");
        }
        log.info("╚═════════════════════════════════════════════════════════════════════════════╝");
    }
}