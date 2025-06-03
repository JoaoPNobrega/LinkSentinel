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
import java.util.List; // Mudado de Map para List para consistência com a sugestão original de preenchimento do Map
import java.util.Map;
import java.util.function.Function; // Para Collectors.toMap
import java.util.stream.Collectors; // Para Collectors.toMap

@Service
@Slf4j
public class LinkVerificationService {

    private final LinkRepository linkRepository;
    private final CheckResultRepository checkResultRepository;
    private final UserRepository userRepository;
    private final Map<VerificationStrategyType, VerificationStrategy> strategies; // Mapa de todas as estratégias (Tipo Enum como chave)

    @Autowired
    public LinkVerificationService(LinkRepository linkRepository,
                                   CheckResultRepository checkResultRepository,
                                   UserRepository userRepository,
                                   List<VerificationStrategy> strategyList) { // Injeta uma Lista de todas as beans de estratégia
        this.linkRepository = linkRepository;
        this.checkResultRepository = checkResultRepository;
        this.userRepository = userRepository;
        // Preenche o mapa usando o VerificationStrategyType enum como chave
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(VerificationStrategy::getType, Function.identity()));
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
                    // Se Link tem um construtor que aceita URL e User, ou se você quer associar o criador aqui.
                    // Se não, apenas new Link(trimmedUrl) é suficiente e a associação com User pode ser feita de outra forma se necessário.
                    Link newLink = new Link(trimmedUrl); 
                    // newLink.setCreatedBy(user); // Exemplo, se Link tiver essa associação
                    return linkRepository.save(newLink);
                });
        
        // Assumindo que Link.java tem setLastChecked
        link.setLastChecked(LocalDateTime.now()); 

        // CORREÇÃO: Usar o builder para criar CheckResult.
        // Isso requer que CheckResult.java tenha um campo 'private User user;' e que o Lombok @Builder esteja presente.
        CheckResult initialCheckResult = CheckResult.builder()
                                            .link(link)
                                            .user(user) // Certifique-se que CheckResult tem este campo e está no builder
                                            .checkTimestamp(LocalDateTime.now()) // Boa prática inicializar aqui
                                            .build();

        // *** USA O PADRÃO STRATEGY AQUI ***
        // CORREÇÃO: Obter estratégia usando o Enum diretamente como chave
        VerificationStrategy strategy = strategies.get(strategyType); 
        if (strategy == null) {
            log.error("Estratégia de verificação não encontrada para o tipo: {}", strategyType);
            // Considerar lançar uma exceção mais específica ou ter uma estratégia padrão.
            throw new IllegalArgumentException("Tipo de estratégia de verificação inválido: " + strategyType);
        }

        // Assumindo que a assinatura de strategy.execute é (CheckResult partialResult, String url)
        // e que ela MODIFICA o partialResult ou retorna um NOVO CheckResult com os dados da verificação.
        // Se strategy.execute popula o initialCheckResult diretamente:
        strategy.execute(initialCheckResult, trimmedUrl); 
        CheckResult finalResult = initialCheckResult; // Se execute modifica o objeto passado
        
        // Se strategy.execute retorna um novo objeto (menos comum se já passamos um):
        // CheckResult finalResultFromStrategy = strategy.execute(initialCheckResult, trimmedUrl);

        // Salva o resultado final da verificação.
        // Certifique-se que todos os campos obrigatórios de CheckResult estão preenchidos pela estratégia
        // (ex: statusCode, accessible).
        CheckResult savedResult = checkResultRepository.save(finalResult);
        log.info("Verificação (estratégia {}) para {} salva com ID {}", strategyType, trimmedUrl, savedResult.getId());

        // Assumindo que Link.java tem addCheckResult para manter a relação bidirecional
        if (link.getCheckResults() != null) { // Evitar NullPointerException se a lista não for inicializada
            link.addCheckResult(savedResult);
        }
        linkRepository.save(link);

        return savedResult;
    }

    @Transactional
    public CheckResult performCheck(String url, String username) {
        // Usa REDIRECT_CHECK como padrão se nenhum strategyType for especificado.
        return performCheck(url, username, VerificationStrategyType.REDIRECT_CHECK); 
    }
}