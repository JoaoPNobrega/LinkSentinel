package br.cesar.school.linksentinel.service.strategy;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.service.verifier.BaseHttpVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("basicHttpStrategy") // Nome do bean para injeção
@RequiredArgsConstructor
public class BasicHttpStrategy implements VerificationStrategy {

    private final BaseHttpVerifier baseHttpVerifier;

    @Override
    public CheckResult execute(CheckResult checkResult, String url) {
        // Esta estratégia usa apenas a verificação HTTP base
        return baseHttpVerifier.verify(checkResult, url);
    }
}