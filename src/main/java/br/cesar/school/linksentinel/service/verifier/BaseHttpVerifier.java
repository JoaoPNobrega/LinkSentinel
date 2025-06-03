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

    private final HttpClient httpClient;

    public BaseHttpVerifier() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public CheckResult verify(CheckResult checkResult, String url) {
        log.info("Iniciando verificação HTTP (HttpClient) para: {}", url);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            checkResult.setStatusCode(response.statusCode());
            checkResult.setFinalUrl(url);

            checkResult.setAccessible(true);

            if (response.statusCode() >= 300 && response.statusCode() < 400) {
                response.headers().firstValue("Location").ifPresent(location -> {
                    checkResult.setFinalUrl(location); 
                    log.info("Redirect detectado. Location header: {}", location);
                });
            }

        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            log.error("Erro ao verificar (HttpClient) {}: {}", url, errorMsg, e);
            checkResult.setAccessible(false);
            checkResult.setFailureReason("Erro: " + errorMsg);
            checkResult.setStatusCode(0);
        }

        log.info("Verificação HTTP (HttpClient) para {} concluída com status {}", url, checkResult.getStatusCode());


        return checkResult;
    }
}