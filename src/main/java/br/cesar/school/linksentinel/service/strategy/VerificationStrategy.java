package br.cesar.school.linksentinel.service.strategy;

import br.cesar.school.linksentinel.model.CheckResult;

public interface VerificationStrategy {
    /**
     *
     * @param checkResult
     * @param url
     * @return 
     */
    CheckResult execute(CheckResult checkResult, String url);

    /**
     * 
     * @return
     */
    VerificationStrategyType getType();
}