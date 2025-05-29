// Local: src/main/java/br/cesar/school/linksentinel/service/ThreatChecker.java
package br.cesar.school.linksentinel.service;

import java.util.List;
import java.util.Optional;

public interface ThreatChecker {
    /**
     * Verifica uma URL contra um serviço externo de detecção de ameaças.
     * @param urlToCheck A URL para verificar.
     * @return Um Optional contendo uma lista de tipos de ameaça (String) se encontradas,
     * ou Optional.empty() se a URL for considerada segura pelo serviço
     * ou se ocorrer um erro na consulta.
     * Uma lista vazia significa que a consulta foi bem-sucedida e nenhuma ameaça foi encontrada.
     */
    Optional<List<String>> checkUrlForThreats(String urlToCheck);
}