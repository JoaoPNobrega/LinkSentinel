package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.CheckResultRepository;
import br.cesar.school.linksentinel.repository.LinkRepository;
import br.cesar.school.linksentinel.repository.UserRepository;
import br.cesar.school.linksentinel.service.strategy.VerificationStrategy; // Importa a Interface Strategy
import br.cesar.school.linksentinel.service.strategy.VerificationStrategyType; // Importa o Enum
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map; // Importa Map

@Service
@Slf4j
public class LinkVerificationService {

    private final LinkRepository linkRepository;
    private final CheckResultRepository checkResultRepository;
    private final UserRepository userRepository;
    private final Map<String, VerificationStrategy> strategies; // Mapa de todas as estratégias

    @Autowired
    public LinkVerificationService(LinkRepository linkRepository,
                                   CheckResultRepository checkResultRepository,
                                   UserRepository userRepository,
                                   Map<String, VerificationStrategy> strategies) {
        this.linkRepository = linkRepository;
        this.checkResultRepository = checkResultRepository;
        this.userRepository = userRepository;
        this.strategies = strategies;
    }

    @Transactional
    public CheckResult performCheck(String url, String username, VerificationStrategyType strategyType) {
        log.info("Iniciando verificação para {} por {} usando estratégia {}", url, username, strategyType);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        String trimmedUrl = url.trim();
        Link link = linkRepository.findByUrl(trimmedUrl)
                .orElseGet(() -> {
                    log.info("Link {} não encontrado, criando novo.", trimmedUrl);
                    return linkRepository.save(new Link(trimmedUrl));
                });
        link.setLastChecked(LocalDateTime.now());

        CheckResult checkResult = new CheckResult(link, user);

        // *** USA O PADRÃO STRATEGY AQUI ***
        VerificationStrategy strategy = strategies.get(strategyType.getBeanName());
        if (strategy == null) {
            log.error("Estratégia de verificação não encontrada para o tipo: {}", strategyType);
            throw new IllegalArgumentException("Tipo de estratégia de verificação inválido: " + strategyType);
        }

        CheckResult finalResult = strategy.execute(checkResult, trimmedUrl);
        // *** FIM DO USO DO STRATEGY ***

        CheckResult savedResult = checkResultRepository.save(finalResult);
        log.info("Verificação (estratégia {}) para {} salva com ID {}", strategyType, trimmedUrl, savedResult.getId());

        link.addCheckResult(savedResult);
        linkRepository.save(link);

        return savedResult;
    }

    @Transactional
    public CheckResult performCheck(String url, String username) {
        return performCheck(url, username, VerificationStrategyType.REDIRECT_CHECK);
    }
}