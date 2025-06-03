package br.cesar.school.linksentinel.service.verifier;

import br.cesar.school.linksentinel.model.CheckResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class RedirectVerifierDecorator extends AbstractVerifierDecorator {

    private static final int MAX_REDIRECTS = 10;

    public RedirectVerifierDecorator(LinkVerifier wrappedVerifier) {
        super(wrappedVerifier);
    }

    @Override
    public CheckResult verify(CheckResult checkResult, String url) throws IOException, InterruptedException {
        log.info("Iniciando verificação COM redirects para: {}", url);

        List<String> redirectChainList = new ArrayList<>();
        String currentUrl = url;
        CheckResult lastIterationResult = null;
        int redirectCount = 0;

        while (redirectCount < MAX_REDIRECTS) {
            CheckResult tempResult = CheckResult.builder()
                    .link(checkResult.getLink())
                    .user(checkResult.getUser())
                    .originalUrl(checkResult.getOriginalUrl())
                    .status(checkResult.getStatus())
                    .checkTimestamp(checkResult.getCheckTimestamp())
                    .build();
            
            tempResult = super.verify(tempResult, currentUrl);

            lastIterationResult = tempResult;
            int statusCode = tempResult.getStatusCode();
            redirectChainList.add(currentUrl + " (" + statusCode + ")");

            if (statusCode >= 300 && statusCode < 400 && tempResult.getFinalUrl() != null && !tempResult.getFinalUrl().isEmpty() && !tempResult.getFinalUrl().equals(currentUrl)) {
                log.debug("Redirect de {} para {}", currentUrl, tempResult.getFinalUrl());
                currentUrl = tempResult.getFinalUrl();
                redirectCount++;
            } else {
                break;
            }
        }

        if (lastIterationResult == null) {
            log.error("lastIterationResult é nulo após o loop de redirecionamento para URL: {}. O checkResult original não será totalmente atualizado.", url);
            checkResult.setAccessible(false);
            checkResult.setFailureReason(checkResult.getFailureReason() == null ? "Falha no processo de redirecionamento" : checkResult.getFailureReason() + "; Falha no processo de redirecionamento");
            checkResult.setFinalUrl(currentUrl);
        } else {
            checkResult.setStatusCode(lastIterationResult.getStatusCode());
            checkResult.setAccessible(lastIterationResult.isAccessible());
            checkResult.setFinalUrl(currentUrl);
            checkResult.setFailureReason(lastIterationResult.getFailureReason());
            checkResult.setStatus(lastIterationResult.getStatus());
        }

        if (redirectCount >= MAX_REDIRECTS) {
            log.warn("Máximo de redirects ({}) atingido para {}", MAX_REDIRECTS, url);
            String existingFailureReason = checkResult.getFailureReason();
            String redirectError = "Máximo de redirects atingido.";
            checkResult.setFailureReason(existingFailureReason == null || existingFailureReason.isEmpty() ? redirectError : existingFailureReason + "; " + redirectError);
            checkResult.setAccessible(false); 
        }
        
        log.info("Verificação COM redirects para {} concluída. URL Final: {}", url, checkResult.getFinalUrl());
        return checkResult;
    }
}