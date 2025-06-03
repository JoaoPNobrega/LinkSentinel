package br.cesar.school.linksentinel.config;

import br.cesar.school.linksentinel.service.observer.LinkStatusObserver;
import br.cesar.school.linksentinel.service.observer.LoggingObserver;
import br.cesar.school.linksentinel.service.strategy.BasicHttpStrategy;
import br.cesar.school.linksentinel.service.strategy.RedirectCheckStrategy;
import br.cesar.school.linksentinel.service.strategy.VerificationStrategy;
import br.cesar.school.linksentinel.service.verifier.BaseHttpVerifier;
import br.cesar.school.linksentinel.service.verifier.LinkVerifier;
import br.cesar.school.linksentinel.service.verifier.RedirectVerifierDecorator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableScheduling
public class AppConfig {

    @Value("${google.safe.Browse.api.key:}")
    private String googleSafeBrowseApiKey;

    @Value("${http.client.connectTimeout:5000}")
    private int connectTimeout;

    @Value("${http.client.readTimeout:10000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return new RestTemplate(factory);
    }

    @Bean
    public BaseHttpVerifier baseHttpVerifier() {
        return new BaseHttpVerifier();
    }

    @Bean
    public BasicHttpStrategy basicHttpStrategy(BaseHttpVerifier verifier) {
        return new BasicHttpStrategy(verifier);
    }

    @Bean
    public RedirectCheckStrategy redirectCheckStrategy(BaseHttpVerifier verifier) {
        return new RedirectCheckStrategy(verifier);
    }

    @Bean
    public List<VerificationStrategy> verificationStrategies(BasicHttpStrategy basicHttpStrategy, RedirectCheckStrategy redirectCheckStrategy) {
        return Arrays.asList(basicHttpStrategy, redirectCheckStrategy);
    }

    @Bean
    @Primary
    public LinkVerifier linkVerifier(BaseHttpVerifier actualBaseHttpVerifier) {
        LinkVerifier verifier = actualBaseHttpVerifier;
        verifier = new RedirectVerifierDecorator(verifier);
        return verifier;
    }
}