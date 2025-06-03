package br.cesar.school.linksentinel.service.verifier;

import br.cesar.school.linksentinel.model.CheckResult;
import java.io.IOException;

public interface LinkVerifier {
    CheckResult verify(CheckResult checkResult, String url) throws IOException, InterruptedException;
}