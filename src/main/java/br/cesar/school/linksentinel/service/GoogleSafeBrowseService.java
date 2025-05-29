// Local: src/main/java/br/cesar/school/linksentinel/service/GoogleSafeBrowseService.java
package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.dto.safeBrowse.ClientInfo;
import br.cesar.school.linksentinel.dto.safeBrowse.ThreatEntry;
import br.cesar.school.linksentinel.dto.safeBrowse.ThreatInfo;
import br.cesar.school.linksentinel.dto.safeBrowse.ThreatMatchRequest;
import br.cesar.school.linksentinel.dto.safeBrowse.ThreatMatchResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("realSafeBrowseService") // Dando um nome específico ao bean
@Slf4j
public class GoogleSafeBrowseService implements ThreatChecker {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String safeBrowseApiUrl;

    public GoogleSafeBrowseService(RestTemplate restTemplate,
                                   @Value("${google.safeBrowse.apikey}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.safeBrowseApiUrl = "https://safeBrowse.googleapis.com/v4/threatMatches:find?key=" + this.apiKey;
    }

    @Override
    public Optional<List<String>> checkUrlForThreats(String urlToCheck) {
        // Ajuste na verificação da chave API para ser mais específico
        if (apiKey == null || apiKey.isEmpty() || "SUA_CHAVE_API_AQUI".equals(apiKey)) {
            log.warn("Chave da API do Google Safe Browse não configurada ou ainda é o valor placeholder 'SUA_CHAVE_API_AQUI'. A verificação do Safe Browse não será realizada.");
            return Optional.empty(); // Retorna empty para indicar que a verificação não pôde ser feita
        }

        ClientInfo clientInfo = new ClientInfo("linksentinel", "1.0.0");
        ThreatEntry threatEntry = new ThreatEntry(urlToCheck);
        ThreatInfo threatInfo = new ThreatInfo(
                List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"),
                List.of("ANY_PLATFORM"),
                List.of("URL"),
                List.of(threatEntry)
        );
        ThreatMatchRequest requestPayload = new ThreatMatchRequest(clientInfo, threatInfo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ThreatMatchRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            log.info("Consultando Google Safe Browse para: {}", urlToCheck);
            ThreatMatchResponse response = restTemplate.postForObject(safeBrowseApiUrl, entity, ThreatMatchResponse.class);

            if (response != null && response.getMatches() != null && !response.getMatches().isEmpty()) {
                List<String> threats = response.getMatches().stream()
                        .map(ThreatMatchResponse.Match::getThreatType)
                        .distinct()
                        .collect(Collectors.toList());
                log.warn("Ameaças encontradas para {}: {}", urlToCheck, threats);
                return Optional.of(threats);
            } else {
                log.info("Nenhuma ameaça encontrada para {} pelo Google Safe Browse.", urlToCheck);
                return Optional.of(Collections.emptyList()); // Nenhuma ameaça
            }
        } catch (Exception e) {
            log.error("Erro ao consultar Google Safe Browse para {}: {}", urlToCheck, e.getMessage(), e);
            return Optional.empty(); // Erro na consulta
        }
    }
}