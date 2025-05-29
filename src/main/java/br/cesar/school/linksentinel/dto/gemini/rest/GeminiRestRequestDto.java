package br.cesar.school.linksentinel.dto.gemini.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiRestRequestDto {
    private List<GeminiContentDto> contents;
    private GeminiGenerationConfigDto generationConfig;
    private List<GeminiSafetySettingDto> safetySettings;
}