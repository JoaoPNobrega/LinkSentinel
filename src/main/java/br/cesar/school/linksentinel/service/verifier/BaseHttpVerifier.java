package br.cesar.school.linksentinel.service.verifier;

import br.cesar.school.linksentinel.model.CheckResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
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
    public CheckResult verify(CheckResult checkResult, String url) throws IOException, InterruptedException {
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

        } catch (HttpTimeoutException e) {
            log.error("Timeout ao verificar (HttpClient) {}: {}", url, e.getMessage());
            checkResult.setAccessible(false);
            checkResult.setFailureReason("Timeout: " + e.getMessage());
            checkResult.setStatusCode(0);
            throw new SocketTimeoutException("Timeout during HTTP request: " + e.getMessage());
        } catch (ConnectException e) {
            log.error("Erro de conexão ao verificar (HttpClient) {}: {}", url, e.getMessage());
            checkResult.setAccessible(false);
            checkResult.setFailureReason("Connection Error: " + e.getMessage());
            checkResult.setStatusCode(0);
            throw e;
        } catch (IOException e) {
            log.error("Erro de I/O ao verificar (HttpClient) {}: {}", url, e.getMessage());
            checkResult.setAccessible(false);
            checkResult.setFailureReason("I/O Error: " + e.getMessage());
            checkResult.setStatusCode(0);
            throw e;
        } catch (InterruptedException e) {
            log.error("Verificação (HttpClient) interrompida para {}: {}", url, e.getMessage());
            checkResult.setAccessible(false);
            checkResult.setFailureReason("Interrupted: " + e.getMessage());
            checkResult.setStatusCode(0);
            Thread.currentThread().interrupt();
            throw e;
        } catch (IllegalArgumentException e) { // Modificado: Apenas IllegalArgumentException
            log.error("URL inválida para {}: {}", url, e.getMessage());
            checkResult.setAccessible(false);
            checkResult.setFailureReason("Invalid URL: " + e.getMessage());
            checkResult.setStatusCode(0);
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            log.error("Erro genérico ao verificar (HttpClient) {}: {}", url, errorMsg, e);
            checkResult.setAccessible(false);
            checkResult.setFailureReason("Generic Error: " + errorMsg);
            checkResult.setStatusCode(0);
        }
        log.info("Verificação HTTP (HttpClient) para {} concluída com status {}", url, checkResult.getStatusCode());
        return checkResult;
    }
}