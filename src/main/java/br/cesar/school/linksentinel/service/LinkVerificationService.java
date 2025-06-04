package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.CheckResultRepository;
import br.cesar.school.linksentinel.repository.LinkRepository;
import br.cesar.school.linksentinel.repository.UserRepository;
import br.cesar.school.linksentinel.service.strategy.VerificationStrategy;
import br.cesar.school.linksentinel.service.strategy.VerificationStrategyType;
import br.cesar.school.linksentinel.service.verifier.LinkVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class LinkVerificationService {

    private static final Logger logger = Logger.getLogger(LinkVerificationService.class.getName());

    private final List<VerificationStrategy> strategies;
    private final CheckResultRepository checkResultRepository;
    private final LinkVerifier configuredVerifier;
    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    @Value("${link.verification.max-retries:2}")
    private int maxRetries;

    @Value("${link.verification.retry-interval-ms:5000}")
    private long retryIntervalMs;

    @Autowired
    public LinkVerificationService(List<VerificationStrategy> strategies,
                                   CheckResultRepository checkResultRepository,
                                   LinkVerifier configuredVerifier,
                                   LinkRepository linkRepository,
                                   UserRepository userRepository) {
        this.strategies = strategies;
        this.checkResultRepository = checkResultRepository;
        this.configuredVerifier = configuredVerifier;
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CheckResult performCheck(String url, String username, VerificationStrategyType strategyType) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        Link link = linkRepository.findByUrl(url)
                .orElseGet(() -> {
                    Link newLink = new Link(url);
                    newLink.setId(UUID.randomUUID());
                    return linkRepository.save(newLink);
                });

        CheckResult initialCheckResult = CheckResult.builder()
                .link(link)
                .user(user)
                .originalUrl(url)
                .build();
        
        CheckResult finalResult = executeVerificationWithRetries(initialCheckResult, url);
        finalResult.setLink(link);
        finalResult.setUser(user);
        return checkResultRepository.save(finalResult);
    }
    
    @Transactional
    public CheckResult verifyLink(Link link) {
        if (link == null || link.getUrl() == null || link.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Link ou URL do link não pode ser nulo/vazio para verificação.");
        }
        
        CheckResult initialCheckResult = CheckResult.builder()
                                            .link(link)
                                            .user(link.getCheckResults() != null && !link.getCheckResults().isEmpty() && link.getCheckResults().get(0) != null ? 
                                                  link.getCheckResults().get(0).getUser() : null)
                                                                                                
                                            .originalUrl(link.getUrl())
                                            .build();

        CheckResult finalResult = executeVerificationWithRetries(initialCheckResult, link.getUrl());
        finalResult.setLink(link); 
        
        if (link.getLastChecked() == null || finalResult.getCheckTimestamp().isAfter(link.getLastChecked())) {
            link.setLastChecked(finalResult.getCheckTimestamp());
        }
        linkRepository.save(link);
        return checkResultRepository.save(finalResult);
    }

    private CheckResult executeVerificationWithRetries(CheckResult initialCheckResultSeed, String urlToVerify) {
        CheckResult currentAttemptResult = null;
        int attempts = 0;
        boolean verificationSuccessAchieved = false;

        while (attempts <= maxRetries && !verificationSuccessAchieved) {
            attempts++;
            CheckResult attemptSeed = CheckResult.builder()
                .link(initialCheckResultSeed.getLink())
                .user(initialCheckResultSeed.getUser())
                .originalUrl(urlToVerify)
                .checkTimestamp(LocalDateTime.now())
                .build();

            logger.log(Level.INFO, "Attempt {0} to verify URL: {1}", new Object[]{attempts, urlToVerify});
            
            try {
                currentAttemptResult = configuredVerifier.verify(attemptSeed, urlToVerify);

                for (VerificationStrategy strategy : strategies) {
                    strategy.execute(currentAttemptResult, urlToVerify);
                }
                
                if (currentAttemptResult.getStatusCode() != 0 && currentAttemptResult.getStatusCode() < 500 && currentAttemptResult.isAccessible()) {
                    verificationSuccessAchieved = true;
                } else if (currentAttemptResult.getStatusCode() >= 500 || !currentAttemptResult.isAccessible()) {
                    logger.log(Level.WARNING, "Verification attempt {0}/{1} for URL {2} failed with status: {3} or inaccessible. Retrying...",
                            new Object[]{attempts, maxRetries + 1, urlToVerify, currentAttemptResult.getStatusCode()});
                }

            } catch (SocketTimeoutException e) {
                logger.log(Level.WARNING, "Attempt {0}/{1} for URL {2} timed out: {3}",
                        new Object[]{attempts, maxRetries + 1, urlToVerify, e.getMessage()});
                currentAttemptResult = attemptSeed;
                currentAttemptResult.setStatus("TIMEOUT");
                currentAttemptResult.setStatusCode(0);
                currentAttemptResult.setAccessible(false);
                currentAttemptResult.setFailureReason("Connection timed out: " + e.getMessage());
                currentAttemptResult.setFinalUrl(urlToVerify);

            } catch (IOException e) {
                logger.log(Level.WARNING, "Attempt {0}/{1} for URL {2} failed with IOException: {3}",
                        new Object[]{attempts, maxRetries + 1, urlToVerify, e.getMessage()});
                currentAttemptResult = attemptSeed;
                currentAttemptResult.setStatus("IO_ERROR");
                currentAttemptResult.setStatusCode(0);
                currentAttemptResult.setAccessible(false);
                currentAttemptResult.setFailureReason("IOException: " + e.getMessage());
                currentAttemptResult.setFinalUrl(urlToVerify);
            }  catch (Exception e) {
                 logger.log(Level.SEVERE, "Attempt {0}/{1} for URL {2} failed with unexpected Exception: {3}",
                        new Object[]{attempts, maxRetries + 1, urlToVerify, e.getMessage(), e});
                currentAttemptResult = attemptSeed;
                currentAttemptResult.setStatus("UNEXPECTED_ERROR");
                currentAttemptResult.setStatusCode(0);
                currentAttemptResult.setAccessible(false);
                currentAttemptResult.setFailureReason("Unexpected error: " + e.getMessage());
                currentAttemptResult.setFinalUrl(urlToVerify);
            }


            if (!verificationSuccessAchieved && attempts <= maxRetries) {
                try {
                    logger.log(Level.INFO, "Retrying URL {0} in {1}ms...", new Object[]{urlToVerify, retryIntervalMs});
                    Thread.sleep(retryIntervalMs);
                } catch (InterruptedException ie) {
                    logger.log(Level.SEVERE, "Retry attempt interrupted for URL: {0}", urlToVerify);
                    Thread.currentThread().interrupt();
                    if (currentAttemptResult == null) currentAttemptResult = attemptSeed;
                    currentAttemptResult.setStatus("INTERRUPTED_RETRY");
                    currentAttemptResult.setFailureReason( (currentAttemptResult.getFailureReason() == null ? "" : currentAttemptResult.getFailureReason() ) + " - Retry process interrupted.");
                    break; 
                }
            }
        }

        if (currentAttemptResult == null) {
            logger.log(Level.SEVERE, "Result is null after all attempts for URL: {0}. Creating default error result.", urlToVerify);
            currentAttemptResult = initialCheckResultSeed; // Fallback
            currentAttemptResult.setCheckTimestamp(LocalDateTime.now());
            currentAttemptResult.setStatus("UNKNOWN_ERROR_NO_RESULT");
            currentAttemptResult.setStatusCode(0);
            currentAttemptResult.setAccessible(false);
            currentAttemptResult.setFailureReason("Verification failed and no result was generated after " + attempts + " attempts.");
            currentAttemptResult.setFinalUrl(urlToVerify);
        }
        
        if (currentAttemptResult.getCheckTimestamp() == null) {
            currentAttemptResult.setCheckTimestamp(LocalDateTime.now());
        }
         if (initialCheckResultSeed.getLink() != null && currentAttemptResult.getLink() == null) {
            currentAttemptResult.setLink(initialCheckResultSeed.getLink());
        }
        if (initialCheckResultSeed.getUser() != null && currentAttemptResult.getUser() == null) {
            currentAttemptResult.setUser(initialCheckResultSeed.getUser());
        }

        return currentAttemptResult;
    }
}