package br.cesar.school.linksentinel.service.verifier;

import br.cesar.school.linksentinel.model.CheckResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractVerifierDecorator implements LinkVerifier {

    protected final LinkVerifier wrappedVerifier;

    @Override
    public CheckResult verify(CheckResult checkResult, String url) {
        // Primeiro, executa o verificador "embrulhado"
        return wrappedVerifier.verify(checkResult, url);
    }
}