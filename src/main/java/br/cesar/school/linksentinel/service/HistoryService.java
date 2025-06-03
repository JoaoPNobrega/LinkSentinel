// Local: src/main/java/br/cesar/school/linksentinel/service/HistoryService.java
package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.CheckResultRepository;
import br.cesar.school.linksentinel.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException; // Importar
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar

import java.util.List;
import java.util.UUID; // Importar

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final CheckResultRepository checkResultRepository;
    private final UserRepository userRepository;

    public List<CheckResult> getHistoryForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        return checkResultRepository.findByUserOrderByCheckTimestampDesc(user);
    }

    // *** NOVO MÉTODO PARA DELETAR UM CHECKRESULT ESPECÍFICO ***
    @Transactional
    public void deleteCheckResult(Long checkResultId) {
        if (!checkResultRepository.existsById(checkResultId)) {
            throw new EntityNotFoundException("Resultado de verificação não encontrado com ID: " + checkResultId);
        }
        checkResultRepository.deleteById(checkResultId);
    }

    // *** NOVO MÉTODO PARA LIMPAR TODO O HISTÓRICO DE UM USUÁRIO ***
    @Transactional
    public void deleteAllHistoryForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        checkResultRepository.deleteByUser(user);
    }
}