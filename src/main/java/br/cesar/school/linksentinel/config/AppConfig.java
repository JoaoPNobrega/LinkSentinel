// Local: src/main/java/br/cesar/school/linksentinel/config/AppConfig.java
package br.cesar.school.linksentinel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration // Indica que esta é uma classe de configuração do Spring
public class AppConfig {

    @Bean // Define este método como um provedor de um bean gerenciado pelo Spring
    public RestTemplate restTemplate() {
        // Aqui você pode adicionar configurações ao RestTemplate se necessário no futuro
        // (ex: timeouts, message converters, error handlers)
        // Por enquanto, um RestTemplate padrão é suficiente.
        return new RestTemplate();
    }

    // Você pode adicionar outros @Bean aqui no futuro, se precisar.
}