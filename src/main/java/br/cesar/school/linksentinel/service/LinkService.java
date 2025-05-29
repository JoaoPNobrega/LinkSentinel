package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.repository.LinkRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;

    @Transactional // Garante que a operação seja atômica
    public Link toggleLinkMonitoring(UUID linkId) {
        Link link = linkRepository.findById(linkId)
                .orElseThrow(() -> new EntityNotFoundException("Link não encontrado com ID: " + linkId));

        link.setMonitored(!link.isMonitored()); // Alterna o status
        return linkRepository.save(link);
    }

    // Poderíamos adicionar outros métodos aqui no futuro, como:
    // public List<Link> getMonitoredLinks() { ... }
}