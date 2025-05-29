// Local: src/main/java/br/cesar/school/linksentinel/service/verifier/SafeBrowseVerifierDecorator.java
package br.cesar.school.linksentinel.service.verifier;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.service.ThreatChecker; // <-- MUDANÇA AQUI
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class SafeBrowseVerifierDecorator extends AbstractVerifierDecorator {

    private final ThreatChecker safeBrowseChecker; // <-- MUDANÇA AQUI

    // O construtor agora recebe a interface ThreatChecker
    public SafeBrowseVerifierDecorator(LinkVerifier wrappedVerifier, ThreatChecker safeBrowseChecker) {
        super(wrappedVerifier);
        this.safeBrowseChecker = safeBrowseChecker;
    }

    @Override
    public CheckResult verify(CheckResult checkResult, String url) {
        CheckResult result = super.verify(checkResult, url);

        String urlToVerify = result.getFinalUrl() != null ? result.getFinalUrl() : url;
        log.info("Decorator: Executando verificação Safe Browse para: {}", urlToVerify);

        // Usa a interface ThreatChecker (que será o Proxy)
        var threatsOptional = safeBrowseChecker.checkUrlForThreats(urlToVerify);

        if (threatsOptional.isPresent()) {
            List<String> threats = threatsOptional.get();
            if (threats.isEmpty()) {
                result.setSafeBrowseOk(true);
                result.setSafeBrowseThreats(null);
                log.info("Decorator: Safe Browse OK para: {}", urlToVerify);
            } else {
                result.setSafeBrowseOk(false);
                result.setSafeBrowseThreats(String.join(", ", threats));
                log.warn("Decorator: Safe Browse detectou ameaças para {}: {}", urlToVerify, result.getSafeBrowseThreats());
            }
        } else {
            log.warn("Decorator: A consulta ao Safe Browse para {} não retornou dados conclusivos (erro na API ou chave).", urlToVerify);
            result.setSafeBrowseOk(null);
            result.setSafeBrowseThreats("Erro ou impossível verificar com Safe Browse");
        }
        return result;
    }
}