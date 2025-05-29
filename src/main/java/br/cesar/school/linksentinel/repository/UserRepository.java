package br.cesar.school.linksentinel.repository;

import br.cesar.school.linksentinel.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID; // Use Long se mudou o ID na entidade User

@Repository // Marca como um componente Spring (Repositório)
public interface UserRepository extends JpaRepository<User, UUID> { // Estende JpaRepository<TipoDaEntidade, TipoDoId>

    // O Spring Data JPA criará automaticamente a implementação deste método
    // Ele é essencial para o Spring Security encontrar um usuário pelo nome.
    Optional<User> findByUsername(String username);

    // Opcional: Método para encontrar por email (útil no registro)
    Optional<User> findByEmail(String email);

}