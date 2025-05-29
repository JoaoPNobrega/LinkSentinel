package br.cesar.school.linksentinel.dto.gemini.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiCandidateDto {
    private GeminiContentDto content;
    private String finishReason;
    private List<GeminiSafetyRatingDto> safetyRatings;
    // private Double tokenCount; // Se precisar
}