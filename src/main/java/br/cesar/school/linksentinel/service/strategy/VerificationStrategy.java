package br.cesar.school.linksentinel.service.strategy;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.model.User;

public interface VerificationStrategy {
    /**
     * Executa uma estratégia específica de verificação de link.
     * @param checkResult O objeto CheckResult inicial (geralmente contendo Link e User).
     * @param url A URL a ser verificada (já trimada).
     * @return O CheckResult populado pela estratégia.
     */
    CheckResult execute(CheckResult checkResult, String url);
}