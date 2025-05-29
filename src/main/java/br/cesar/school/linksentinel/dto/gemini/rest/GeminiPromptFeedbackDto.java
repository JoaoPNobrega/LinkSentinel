package br.cesar.school.linksentinel.dto.gemini.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiPromptFeedbackDto {
    private String blockReason;
    private List<GeminiSafetyRatingDto> safetyRatings;
}