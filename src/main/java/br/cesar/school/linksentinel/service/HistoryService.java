package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.CheckResultRepository;
import br.cesar.school.linksentinel.repository.LinkRepository;
import br.cesar.school.linksentinel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HistoryService {

    private final LinkRepository linkRepository;
    private final CheckResultRepository checkResultRepository;
    private final UserRepository userRepository;

    @Value("${history.uptime.check-count:10}")
    private int uptimeCheckCount;

    @Autowired
    public HistoryService(LinkRepository linkRepository,
                          CheckResultRepository checkResultRepository,
                          UserRepository userRepository) {
        this.linkRepository = linkRepository;
        this.checkResultRepository = checkResultRepository;
        this.userRepository = userRepository;
    }

    public List<Link> getAllLinks() {
        return linkRepository.findAll();
    }

    public Optional<Link> getLinkById(UUID id) {
        return linkRepository.findById(id);
    }

    public List<CheckResult> getCheckResultsForLink(Link link) {
        if (link == null) {
            return Collections.emptyList();
        }
        return checkResultRepository.findByLinkOrderByCheckTimestampDesc(link); // Corrigido
    }

    public double calculateUptimePercentage(Link link) {
        if (link == null || uptimeCheckCount <= 0) {
            return 0.0;
        }
        List<CheckResult> recentChecks = checkResultRepository.findFirstNByLinkOrderByCheckTimestampDesc( // Corrigido
                link, PageRequest.of(0, uptimeCheckCount)
        );
        if (recentChecks.isEmpty()) {
            return 0.0;
        }
        long upCount = recentChecks.stream()
                .filter(result -> result.getStatusCode() >= 200 && result.getStatusCode() < 300)
                .count();
        return (double) upCount / recentChecks.size() * 100.0;
    }

    @Transactional(readOnly = true)
    public List<CheckResult> getHistoryForUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return checkResultRepository.findByUserOrderByCheckTimestampDesc(userOptional.get());
        }
        return Collections.emptyList();
    }

    @Transactional
    public void deleteCheckResult(Long id) {
        if (checkResultRepository.existsById(id)) {
            checkResultRepository.deleteById(id);
        }
    }

    @Transactional
    public void deleteAllHistoryForUser(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            checkResultRepository.deleteByUser(userOptional.get());
        }
    }
}