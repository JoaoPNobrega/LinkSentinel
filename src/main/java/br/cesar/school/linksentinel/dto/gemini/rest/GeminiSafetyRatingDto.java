package br.cesar.school.linksentinel.dto.gemini.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiSafetyRatingDto {
    private String category;
    private String probability; // Ex: "NEGLIGIBLE", "LOW", "MEDIUM", "HIGH"
}