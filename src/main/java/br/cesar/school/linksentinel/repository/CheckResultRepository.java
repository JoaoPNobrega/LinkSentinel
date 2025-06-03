package br.cesar.school.linksentinel.repository;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    List<CheckResult> findByLinkOrderByCheckTimestampDesc(Link link);

    List<CheckResult> findFirstNByLinkOrderByCheckTimestampDesc(Link link, Pageable pageable);

    List<CheckResult> findByUserOrderByCheckTimestampDesc(User user);

    void deleteByUser(User user);

    List<CheckResult> findTop2ByLinkOrderByCheckTimestampDesc(Link link);

    long countByUser(User user);
}