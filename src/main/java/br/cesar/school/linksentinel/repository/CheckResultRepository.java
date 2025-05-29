// Local: src/main/java/br/cesar/school/linksentinel/repository/CheckResultRepository.java
package br.cesar.school.linksentinel.repository;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Importar
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckResultRepository extends JpaRepository<CheckResult, UUID> {

    @Query("SELECT cr FROM CheckResult cr JOIN FETCH cr.link JOIN FETCH cr.user WHERE cr.user = :user ORDER BY cr.checkTimestamp DESC")
    List<CheckResult> findByUserOrderByCheckTimestampDesc(@Param("user") User user);

    List<CheckResult> findTop2ByLinkOrderByCheckTimestampDesc(Link link);

    long countByUser(User user);

    // *** NOVO MÉTODO PARA DELETAR HISTÓRICO DO USUÁRIO ***
    @Modifying // Necessário para operações de delete ou update com @Query
    @Query("DELETE FROM CheckResult cr WHERE cr.user = :user")
    void deleteByUser(@Param("user") User user);
}