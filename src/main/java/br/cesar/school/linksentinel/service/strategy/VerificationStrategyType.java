package br.cesar.school.linksentinel.service.strategy;

public enum VerificationStrategyType {
    BASIC_HTTP("basicHttpStrategy"),
    REDIRECT_CHECK("redirectCheckStrategy"); // <<<--- MUDANÇA AQUI: vírgula para ponto e vírgula

    // Futuramente, quando você descomentar as constantes abaixo, 
    // a última da lista ativa deverá terminar com ponto e vírgula,
    // e as anteriores com vírgula.
    // Exemplo:
    // REDIRECT_CHECK("redirectCheckStrategy"), 
    // FULL_SECURITY("fullSecurityStrategy"),
    // SAFE_Browse_ONLY("safeBrowseStrategy"); // Última ativa com ;

    private final String beanName;

    VerificationStrategyType(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}