// Local: src/main/java/br/cesar/school/linksentinel/service/GeminiService.java
package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.dto.gemini.rest.*; // Importa todos os nossos DTOs REST
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.ArrayList; // Para a lista de SafetySettings

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.apikey}")
    private String apiKey;

    private final RestTemplate restTemplate; // Injetado pelo Spring (precisa do @Bean em AppConfig)
    private boolean initialized = false;
    private String modelName = "gemini-1.5-flash-latest"; // Ou "gemini-pro"
    private String apiUrlTemplate;

    // Construtor para injetar RestTemplate
    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty() || "SUA_CHAVE_API_GEMINI_AQUI".equals(apiKey)) {
            log.warn("Chave da API do Gemini não configurada ou é placeholder em application.properties. GeminiService não poderá fazer chamadas.");
            initialized = false;
            return;
        }
        this.apiUrlTemplate = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;
        initialized = true;
        log.info("GeminiService (via REST API) inicializado. Modelo: {}. Endpoint pronto.", modelName);
    }

    public String analyzeUrlForPhishing(String urlToAnalyze) {
        if (!initialized) {
            log.warn("GeminiService não inicializado corretamente (verifique API Key). Análise da URL {} pulada.", urlToAnalyze);
            return "Serviço Gemini não disponível (verifique API Key e logs).";
        }

        String prompt = String.format(
                "Analise a seguinte URL APENAS quanto a possíveis sinais de phishing, engenharia social ou conteúdo enganoso que leve o usuário a fornecer informações sensíveis ou instalar malware: '%s'. " +
                "Responda de forma MUITO concisa, em português, com no máximo duas frases. Comece com 'Risco:' seguido de Baixo, Médio ou Alto. Exemplo: 'Risco: Alto. A URL parece suspeita devido a X e Y.' " +
                "Se a URL parecer segura desses pontos de vista, indique 'Risco: Baixo.'",
                urlToAnalyze
        );

        GeminiPartDto part = new GeminiPartDto(prompt);
        GeminiContentDto content = new GeminiContentDto(List.of(part), "user"); // "user" role for prompt

        GeminiGenerationConfigDto generationConfig = GeminiGenerationConfigDto.builder()
                .temperature(0.7f)
                .topK(1)
                .topP(1.0f)
                .maxOutputTokens(200) // Reduzido para respostas mais curtas e econômicas
                .build();

        List<GeminiSafetySettingDto> safetySettings = new ArrayList<>();
        safetySettings.add(new GeminiSafetySettingDto("HARM_CATEGORY_HARASSMENT", "BLOCK_ONLY_HIGH"));
        safetySettings.add(new GeminiSafetySettingDto("HARM_CATEGORY_HATE_SPEECH", "BLOCK_ONLY_HIGH"));
        safetySettings.add(new GeminiSafetySettingDto("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_ONLY_HIGH"));
        safetySettings.add(new GeminiSafetySettingDto("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_ONLY_HIGH"));

        GeminiRestRequestDto requestDto = new GeminiRestRequestDto(List.of(content), generationConfig, safetySettings);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GeminiRestRequestDto> entity = new HttpEntity<>(requestDto, headers);

        log.info("Enviando prompt para Gemini (API REST) para a URL: {}", urlToAnalyze);

        try {
            GeminiRestResponseDto responseDto = restTemplate.postForObject(apiUrlTemplate, entity, GeminiRestResponseDto.class);

            if (responseDto == null) {
                log.warn("Resposta do Gemini para {} foi nula.", urlToAnalyze);
                return "Resposta do Gemini foi nula.";
            }
            
            if (responseDto.getPromptFeedback() != null && responseDto.getPromptFeedback().getBlockReason() != null) {
                String blockReason = responseDto.getPromptFeedback().getBlockReason();
                log.warn("Prompt para {} bloqueado pelo Gemini. Razão: {}. SafetyRatings: {}", 
                         urlToAnalyze, blockReason, responseDto.getPromptFeedback().getSafetyRatings());
                return "Análise bloqueada pelo Gemini devido a: " + blockReason;
            }

            if (responseDto.getCandidates() != null && !responseDto.getCandidates().isEmpty()) {
                GeminiCandidateDto firstCandidate = responseDto.getCandidates().get(0);
                if (firstCandidate.getContent() != null && !firstCandidate.getContent().getParts().isEmpty()) {
                    String analysis = firstCandidate.getContent().getParts().get(0).getText();
                    log.info("Análise do Gemini para {}: {}", urlToAnalyze, analysis);
                    return analysis.trim();
                }
            }
            
            log.warn("Resposta do Gemini para {} não continha texto de análise esperado. Resposta completa: {}", urlToAnalyze, responseDto);
            return "Resposta do Gemini não continha análise de texto ou estava vazia.";

        } catch (HttpClientErrorException e) {
            log.error("Erro de cliente HTTP ao chamar API Gemini para URL {}: {} - Resposta: {}", urlToAnalyze, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "Erro ao obter análise do Gemini (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Erro geral ao chamar API Gemini para URL {}: {}", urlToAnalyze, e.getMessage(), e);
            return "Erro geral ao obter análise do Gemini: " + e.getMessage();
        }
    }
    // Não precisamos mais do método shutdown() do ExecutorService aqui
}