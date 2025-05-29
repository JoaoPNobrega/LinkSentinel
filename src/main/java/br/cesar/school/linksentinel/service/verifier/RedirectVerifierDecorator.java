package br.cesar.school.linksentinel.service.verifier;

import br.cesar.school.linksentinel.model.CheckResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RedirectVerifierDecorator extends AbstractVerifierDecorator {

    private static final int MAX_REDIRECTS = 10; // Evita loops infinitos

    public RedirectVerifierDecorator(LinkVerifier wrappedVerifier) {
        super(wrappedVerifier);
    }

    @Override
    public CheckResult verify(CheckResult checkResult, String url) {
        log.info("Iniciando verificação COM redirects para: {}", url);

        List<String> redirectChain = new ArrayList<>();
        String currentUrl = url;
        CheckResult currentResult = checkResult;
        int redirectCount = 0;

        while (redirectCount < MAX_REDIRECTS) {
            // Executa a verificação base na URL atual
            currentResult = super.verify(new CheckResult(checkResult.getLink(), checkResult.getUser()), currentUrl); // Usa um novo CheckResult temporário

            int statusCode = currentResult.getHttpStatusCode() != null ? currentResult.getHttpStatusCode() : 0;
            redirectChain.add(currentUrl + " (" + statusCode + ")");

            // Verifica se é um redirect (3xx) e se temos para onde ir
            if (statusCode >= 300 && statusCode < 400 && currentResult.getFinalUrl() != null && !currentResult.getFinalUrl().equals(currentUrl)) {
                log.debug("Redirect de {} para {}", currentUrl, currentResult.getFinalUrl());
                currentUrl = currentResult.getFinalUrl(); // A próxima URL a ser verificada
                redirectCount++;
            } else {
                // Não é redirect ou não tem Location, paramos aqui.
                break;
            }
        }

        if (redirectCount >= MAX_REDIRECTS) {
            log.warn("Máximo de redirects ({}) atingido para {}", MAX_REDIRECTS, url);
            currentResult.setErrorMessage((currentResult.getErrorMessage() == null ? "" : currentResult.getErrorMessage() + "; ") + "Máximo de redirects atingido.");
        }

        // Atualiza o CheckResult original com os dados finais
        checkResult.setHttpStatusCode(currentResult.getHttpStatusCode());
        checkResult.setReachable(currentResult.getReachable());
        checkResult.setResponseTimeMs(currentResult.getResponseTimeMs()); // Pode ser melhor somar os tempos...
        checkResult.setErrorMessage(currentResult.getErrorMessage());
        checkResult.setFinalUrl(currentUrl);
        checkResult.setRedirectChain(String.join(" -> ", redirectChain)); // Guarda a cadeia

        log.info("Verificação COM redirects para {} concluída. URL Final: {}", url, currentUrl);
        return checkResult;
    }
}