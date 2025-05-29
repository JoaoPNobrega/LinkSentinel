// Local: src/main/java/br/cesar/school/linksentinel/service/verifier/GeminiVerifierDecorator.java
package br.cesar.school.linksentinel.service.verifier;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.service.GeminiService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeminiVerifierDecorator extends AbstractVerifierDecorator {

    private final GeminiService geminiService;

    public GeminiVerifierDecorator(LinkVerifier wrappedVerifier, GeminiService geminiService) {
        super(wrappedVerifier);
        this.geminiService = geminiService;
    }

    @Override
    public CheckResult verify(CheckResult checkResult, String url) {
        // Executa o verificador anterior
        CheckResult result = super.verify(checkResult, url);

        String urlToAnalyze = result.getFinalUrl() != null ? result.getFinalUrl() : url;
        log.info("Decorator: Executando análise com Gemini para: {}", urlToAnalyze);

        try {
            String analysis = geminiService.analyzeUrlForPhishing(urlToAnalyze);
            result.setGeminiAnalysisResult(analysis);
        } catch (Exception e) {
            log.error("Decorator: Erro ao chamar GeminiService para {}: {}", urlToAnalyze, e.getMessage(), e);
            result.setGeminiAnalysisResult("Falha ao obter análise do Gemini.");
        }

        return result;
    }
}