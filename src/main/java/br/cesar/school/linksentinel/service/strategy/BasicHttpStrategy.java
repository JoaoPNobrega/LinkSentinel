package br.cesar.school.linksentinel.service.strategy;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.service.verifier.BaseHttpVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component("basicHttpStrategy")
@RequiredArgsConstructor
public class BasicHttpStrategy implements VerificationStrategy {

    private final BaseHttpVerifier baseHttpVerifier;

    @Override
    public CheckResult execute(CheckResult checkResult, String url) throws IOException, InterruptedException {
        return baseHttpVerifier.verify(checkResult, url);
    }

    @Override
    public VerificationStrategyType getType() {
        return VerificationStrategyType.BASIC_HTTP;
    }
}