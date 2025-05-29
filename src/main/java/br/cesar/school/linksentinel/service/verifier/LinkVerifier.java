package br.cesar.school.linksentinel.service.verifier;

import br.cesar.school.linksentinel.model.CheckResult;

public interface LinkVerifier {
    /**
     * Verifica um link e atualiza o CheckResult com as informações.
     * @param checkResult O objeto CheckResult a ser populado.
     * @param url A URL a ser verificada.
     * @return O CheckResult atualizado.
     */
    CheckResult verify(CheckResult checkResult, String url);
}