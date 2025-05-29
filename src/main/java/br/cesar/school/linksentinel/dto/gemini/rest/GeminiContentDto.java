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
public class GeminiContentDto {
    private List<GeminiPartDto> parts;
    private String role; // "user" para prompt, "model" para resposta
}