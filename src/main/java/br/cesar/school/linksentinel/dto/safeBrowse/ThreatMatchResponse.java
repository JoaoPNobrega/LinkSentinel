package br.cesar.school.linksentinel.dto.safeBrowse;

import lombok.Data;
import java.util.List;

@Data
public class ThreatMatchResponse {
    private List<Match> matches;

    @Data
    public static class Match {
        private String threatType;
        private String platformType;
        private String threatEntryType;
        private ThreatEntry threat;
        private String cacheDuration;
    }
}