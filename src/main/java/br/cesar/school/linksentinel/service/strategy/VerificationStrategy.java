package br.cesar.school.linksentinel.service.strategy;

import br.cesar.school.linksentinel.model.CheckResult;
import java.io.IOException;

public interface VerificationStrategy {
    CheckResult execute(CheckResult checkResult, String url) throws IOException, InterruptedException;
    VerificationStrategyType getType();
}