package br.cesar.school.linksentinel.repository;

import br.cesar.school.linksentinel.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkRepository extends JpaRepository<Link, UUID> {

    Optional<Link> findByUrl(String url);

    List<Link> findByMonitoredTrue();

    long countByMonitoredTrue();
}