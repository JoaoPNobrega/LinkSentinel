package br.cesar.school.linksentinel.service.strategy;

public enum VerificationStrategyType {
    BASIC_HTTP("basicHttpStrategy"),
    REDIRECT_CHECK("redirectCheckStrategy");

    private final String beanName;

    VerificationStrategyType(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}