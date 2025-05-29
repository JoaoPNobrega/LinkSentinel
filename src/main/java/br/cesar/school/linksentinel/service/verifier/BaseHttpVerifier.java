package br.cesar.school.linksentinel.service.verifier;

import br.cesar.school.linksentinel.model.CheckResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
public class BaseHttpVerifier implements LinkVerifier {

    // Usando o HttpClient moderno do Java
    private final HttpClient httpClient;

    public BaseHttpVerifier() {
        // Configura o HttpClient para NÃO seguir redirects e ter um timeout
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER) // <-- Não seguir redirects!
                .connectTimeout(Duration.ofSeconds(10)) // Timeout de conexão
                .build();
    }

    @Override
    public CheckResult verify(CheckResult checkResult, String url) {
        log.info("Iniciando verificação HTTP (HttpClient) para: {}", url);
        long startTime = System.currentTimeMillis();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10)) // Timeout da requisição
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            checkResult.setHttpStatusCode(response.statusCode());
            checkResult.setFinalUrl(url); // A URL *desta* requisição

            // É alcançável se recebeu *qualquer* resposta (mesmo 3xx, 4xx, 5xx)
            checkResult.setReachable(true);

            // Se for um redirect (3xx), pegamos o 'Location' header
            if (response.statusCode() >= 300 && response.statusCode() < 400) {
                response.headers().firstValue("Location").ifPresent(checkResult::setFinalUrl);
                log.info("Redirect detectado para: {}", checkResult.getFinalUrl());
            }

        } catch (Exception e) {
            // Se a mensagem for nula, usamos o e.toString() que dá mais detalhes.
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            log.error("Erro ao verificar (HttpClient) {}: {}", url, errorMsg, e); // Loga a exceção completa
            checkResult.setReachable(false);
            checkResult.setErrorMessage("Erro: " + errorMsg); // Usa a nova msg
        }

        long endTime = System.currentTimeMillis();
        checkResult.setResponseTimeMs(endTime - startTime);
        log.info("Verificação HTTP (HttpClient) para {} concluída em {}ms com status {}", url, checkResult.getResponseTimeMs(), checkResult.getHttpStatusCode());

        return checkResult;
    }
}