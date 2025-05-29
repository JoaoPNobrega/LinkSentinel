// Local: src/main/java/br/cesar/school/linksentinel/service/SafeBrowseProxyService.java
package br.cesar.school.linksentinel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("safeBrowseProxy") // Nome do bean para o Proxy
@Slf4j
public class SafeBrowseProxyService implements ThreatChecker {

    private final ThreatChecker realSafeBrowseService;
    // Futuramente, poderíamos injetar um serviço de cache aqui.
    // Ex: private final CacheManager cacheManager;

    public SafeBrowseProxyService(@Qualifier("realSafeBrowseService") ThreatChecker realSafeBrowseService) {
        this.realSafeBrowseService = realSafeBrowseService;
    }

    @Override
    public Optional<List<String>> checkUrlForThreats(String urlToCheck) {
        log.info("Proxy: Verificando URL {} com Safe Browse.", urlToCheck);

        // Lógica de Cache (Exemplo Simplificado - a ser implementado se houver tempo):
        // if (cache.contains(urlToCheck)) {
        // log.info("Proxy: Resultado encontrado no cache para {}", urlToCheck);
        // return cache.get(urlToCheck);
        // }

        // Chamando o serviço real
        Optional<List<String>> result = realSafeBrowseService.checkUrlForThreats(urlToCheck);

        // Lógica de Cache (Salvar resultado):
        // if (result.isPresent()) {
        // cache.put(urlToCheck, result);
        // }
        log.info("Proxy: Verificação de {} concluída.", urlToCheck);
        return result;
    }
}