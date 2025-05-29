package br.cesar.school.linksentinel.dto.safeBrowse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreatInfo {
    private List<String> threatTypes;     // Ex: ["MALWARE", "SOCIAL_ENGINEERING"]
    private List<String> platformTypes;   // Ex: ["ANY_PLATFORM"]
    private List<String> threatEntryTypes; // Ex: ["URL"]
    private List<ThreatEntry> threatEntries;
}