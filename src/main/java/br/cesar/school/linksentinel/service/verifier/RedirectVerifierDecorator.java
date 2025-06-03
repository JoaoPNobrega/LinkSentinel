package br.cesar.school.linksentinel.service.verifier;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.model.User;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RedirectVerifierDecorator extends AbstractVerifierDecorator {

    private static final int MAX_REDIRECTS = 10;

    public RedirectVerifierDecorator(LinkVerifier wrappedVerifier) {
        super(wrappedVerifier);
    }

    @Override
    public CheckResult verify(CheckResult checkResult, String url) {
        log.info("Iniciando verificação COM redirects para: {}", url);

        List<String> redirectChainList = new ArrayList<>();
        String currentUrl = url;
        CheckResult lastIterationResult = null;
        int redirectCount = 0;

        while (redirectCount < MAX_REDIRECTS) {

            CheckResult tempResult = CheckResult.builder()
                                        .link(checkResult.getLink())
                                        .user(checkResult.getUser()) 
                                        .build();
            
            tempResult = super.verify(tempResult, currentUrl);

            lastIterationResult = tempResult;
            int statusCode = tempResult.getStatusCode(); 
            redirectChainList.add(currentUrl + " (" + statusCode + ")");

            if (statusCode >= 300 && statusCode < 400 && tempResult.getFinalUrl() != null && !tempResult.getFinalUrl().equals(currentUrl)) {
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

        }


        if (redirectCount >= MAX_REDIRECTS) {
            log.warn("Máximo de redirects ({}) atingido para {}", MAX_REDIRECTS, url);
            String existingFailureReason = checkResult.getFailureReason();
            String redirectError = "Máximo de redirects atingido.";
            checkResult.setFailureReason(existingFailureReason == null || existingFailureReason.isEmpty() ? redirectError : existingFailureReason + "; " + redirectError);
        }
        
        log.info("Verificação COM redirects para {} concluída. URL Final: {}", url, checkResult.getFinalUrl());
        return checkResult;
    }
}