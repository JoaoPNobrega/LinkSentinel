package br.cesar.school.linksentinel.model;

import jakarta.persistence.*; // Pacote para JPA (Jakarta Persistence)
import lombok.Data; // Lombok para getters, setters, etc.
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID; // Usando UUID para ID, mas pode ser Long

@Data // Gera Getters, Setters, toString, equals, hashCode
@NoArgsConstructor // Gera construtor sem argumentos
@AllArgsConstructor // Gera construtor com todos os argumentos
@Entity // Marca como uma entidade JPA
@Table(name = "users") // Define o nome da tabela no banco
public class User implements UserDetails { // Implementa UserDetails

    @Id // Marca como chave primária
    @GeneratedValue(strategy = GenerationType.AUTO) // Gera o ID automaticamente (UUID)
    private UUID id;

    @Column(nullable = false, unique = true) // Não pode ser nulo, deve ser único
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Armazenaremos a senha HASHED aqui

    // Para simplificar, vamos usar uma string para o papel (ROLE).
    // Em um sistema maior, poderia ser uma entidade separada (ManyToMany).
    private String role = "ROLE_USER";

    // --- Implementação dos métodos do UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retorna a lista de papéis/permissões do usuário
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getPassword() {
        // Retorna a senha (hashed)
        return this.password;
    }

    @Override
    public String getUsername() {
        // Retorna o nome de usuário
        return this.username;
    }

    // Para este exemplo, vamos considerar que as contas nunca expiram/bloqueiam
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}