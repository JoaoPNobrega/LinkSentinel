package br.cesar.school.linksentinel.dto.gemini.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiSafetySettingDto {
    private String category; // Ex: "HARM_CATEGORY_HARASSMENT"
    private String threshold; // Ex: "BLOCK_ONLY_HIGH"
}