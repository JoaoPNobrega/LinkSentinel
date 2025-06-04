package br.cesar.school.linksentinel.service;

import java.util.List;
import java.util.Optional;

public interface ThreatChecker {
    /**
     * 
     * @param urlToCheck 
     * @return 
     * 
     * 
     * 
     */
    Optional<List<String>> checkUrlForThreats(String urlToCheck);
}