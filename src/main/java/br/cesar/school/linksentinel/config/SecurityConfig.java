package br.cesar.school.linksentinel.config;

import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.UserRepository;
import br.cesar.school.linksentinel.view.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Optional;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig extends VaadinWebSecurity {

    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            log.info("UserDetailsService: Tentando carregar usuário: {}", username);
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                log.warn("UserDetailsService: Usuário {} NÃO encontrado no repositório!", username);
            } else {
                log.info("UserDetailsService: Usuário {} encontrado no repositório!", username);
            }
            return userOptional.orElseThrow(() ->
                new UsernameNotFoundException("Usuário não encontrado: " + username));
        };
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/auth/register")).permitAll()
        );

        http.csrf(csrf -> csrf
                .ignoringRequestMatchers(
                        new AntPathRequestMatcher("/api/auth/register"),
                        new AntPathRequestMatcher("/h2-console/**")
                )
        );
        
        super.configure(http);

        setLoginView(http, LoginView.class, "/login");
    }
}