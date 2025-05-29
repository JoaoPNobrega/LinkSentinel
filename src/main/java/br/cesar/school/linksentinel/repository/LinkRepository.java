// Local: src/main/java/br/cesar/school/linksentinel/repository/LinkRepository.java
package br.cesar.school.linksentinel.repository;

import br.cesar.school.linksentinel.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LinkRepository extends JpaRepository<Link, UUID> {

    // Método para encontrar um link pela sua URL (usado no LinkVerificationService)
    Optional<Link> findByUrl(String url);

    // Método para encontrar todos os links que estão marcados para monitoramento (usado no MonitoringService)
    List<Link> findByMonitoredTrue();

    // NOVO MÉTODO: Conta quantos links no total estão marcados como monitorados
    long countByMonitoredTrue();
}