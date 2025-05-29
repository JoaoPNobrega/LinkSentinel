package br.cesar.school.linksentinel.dto.gemini.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder // Facilita a criação
@JsonInclude(JsonInclude.Include.NON_NULL) // Só inclui campos não nulos no JSON
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiGenerationConfigDto {
    private Float temperature;
    private Integer topK;
    private Float topP;
    private Integer maxOutputTokens;
    // private Integer candidateCount; // Se necessário
    // private List<String> stopSequences; // Se necessário
}