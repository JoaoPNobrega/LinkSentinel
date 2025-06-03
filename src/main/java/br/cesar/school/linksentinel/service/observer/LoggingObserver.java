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
        log.info("╔═════════════════════════════════════════════════════════════════════════════╗");
        log.info("║          ALERTA DE MUDANÇA DE STATUS PARA O LINK MONITORADO                 ║");
        log.info("╠═════════════════════════════════════════════════════════════════════════════╣");
        log.info("║ URL: {}", link.getUrl());
        log.info("║ --------------------------------------------------------------------------- ║");

        if (oldResult != null) {
            log.info("║ Status Anterior: HTTP {} | Acessível: {} | Em: {}",
                    oldResult.getStatusCode(), 
                    oldResult.isAccessible() ? "SIM" : "NÃO", 
                    oldResult.getCheckTimestamp());
        } else {
            log.info("║ Status Anterior: Não há registro anterior significativo ou é a primeira verificação após monitorar.");
        }

        log.info("║ Novo Status    : HTTP {} | Acessível: {} | Em: {}",
                newResult.getStatusCode(), // )
                newResult.isAccessible() ? "SIM" : "NÃO",
                newResult.getCheckTimestamp());
        log.info("║ --------------------------------------------------------------------------- ║");

        // Detalhando a mudança
        if (oldResult != null) {
            // Compara booleanos diretamente
            if (oldResult.isAccessible() != newResult.isAccessible()) { 
                log.info("║ MUDANÇA: Acessibilidade mudou de {} para {}",
                        oldResult.isAccessible() ? "SIM" : "NÃO",
                        newResult.isAccessible() ? "SIM" : "NÃO");
            }
            
            if (oldResult.getStatusCode() != newResult.getStatusCode()) { 
                log.info("║ MUDANÇA: Status HTTP mudou de {} para {}",
                        oldResult.getStatusCode(), newResult.getStatusCode()); 
            }
        } else {
            log.info("║ MUDANÇA: Primeira verificação relevante ou link passou a ser monitorado.");
        }
        log.info("╚═════════════════════════════════════════════════════════════════════════════╝");
    }
}