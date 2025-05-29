package br.cesar.school.linksentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // <-- IMPORTAR

/**
 * Classe principal que inicia a aplicação Spring Boot Link Sentinel.
 */
@SpringBootApplication
@EnableScheduling // <-- ADICIONAR ESTA ANOTAÇÃO
public class App
{
    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }
}