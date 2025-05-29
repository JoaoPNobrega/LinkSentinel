package br.cesar.school.linksentinel.dto.safeBrowse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {
    private String clientId; // Ex: "linksentinel"
    private String clientVersion; // Ex: "1.0.0"
}