package br.cesar.school.linksentinel.dto.safeBrowse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreatMatchRequest {
    private ClientInfo client;
    private ThreatInfo threatInfo;
}