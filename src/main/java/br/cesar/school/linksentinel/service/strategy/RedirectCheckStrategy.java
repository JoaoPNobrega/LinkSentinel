package br.cesar.school.linksentinel.service.strategy;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.service.verifier.BaseHttpVerifier;
import br.cesar.school.linksentinel.service.verifier.LinkVerifier;
import br.cesar.school.linksentinel.service.verifier.RedirectVerifierDecorator;
import org.springframework.stereotype.Component;

@Component("redirectCheckStrategy")
public class RedirectCheckStrategy implements VerificationStrategy {

    private final BaseHttpVerifier baseHttpVerifier;

    public RedirectCheckStrategy(BaseHttpVerifier baseHttpVerifier ) {
        this.baseHttpVerifier = baseHttpVerifier;

    }

    @Override
    public CheckResult execute(CheckResult checkResult, String url) {

        LinkVerifier verifier = baseHttpVerifier; 
        verifier = new RedirectVerifierDecorator(verifier); 

        return verifier.verify(checkResult, url);
    }

    @Override
    public VerificationStrategyType getType() { 
        return VerificationStrategyType.REDIRECT_CHECK;
    }
}