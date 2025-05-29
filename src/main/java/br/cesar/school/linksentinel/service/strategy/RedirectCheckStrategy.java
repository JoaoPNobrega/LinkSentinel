// Local: src/main/java/br/cesar/school/linksentinel/service/strategy/RedirectCheckStrategy.java
package br.cesar.school.linksentinel.service.strategy;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.service.GeminiService;
import br.cesar.school.linksentinel.service.ThreatChecker;
import br.cesar.school.linksentinel.service.verifier.BaseHttpVerifier;
import br.cesar.school.linksentinel.service.verifier.GeminiVerifierDecorator;
import br.cesar.school.linksentinel.service.verifier.LinkVerifier;
import br.cesar.school.linksentinel.service.verifier.RedirectVerifierDecorator;
import br.cesar.school.linksentinel.service.verifier.SafeBrowseVerifierDecorator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("redirectCheckStrategy")
public class RedirectCheckStrategy implements VerificationStrategy {

    private final BaseHttpVerifier baseHttpVerifier;
    private final ThreatChecker safeBrowseProxy; // Nosso Proxy para Safe Browse
    private final GeminiService geminiService;     // Nosso Serviço para Gemini

    public RedirectCheckStrategy(BaseHttpVerifier baseHttpVerifier,
                                 @Qualifier("safeBrowseProxy") ThreatChecker safeBrowseProxy,
                                 GeminiService geminiService) {
        this.baseHttpVerifier = baseHttpVerifier;
        this.safeBrowseProxy = safeBrowseProxy;
        this.geminiService = geminiService;
    }

    @Override
    public CheckResult execute(CheckResult checkResult, String url) {
        // Monta a cadeia completa de decoradores
        LinkVerifier verifier = baseHttpVerifier;
        verifier = new RedirectVerifierDecorator(verifier);
        // O SafeBrowseVerifierDecorator usa o Proxy (ThreatChecker)
        verifier = new SafeBrowseVerifierDecorator(verifier, safeBrowseProxy);
        // O GeminiVerifierDecorator usa o GeminiService
        verifier = new GeminiVerifierDecorator(verifier, geminiService);

        return verifier.verify(checkResult, url);
    }
}