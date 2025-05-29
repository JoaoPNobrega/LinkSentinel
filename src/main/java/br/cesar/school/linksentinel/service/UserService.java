package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.dto.RegisterRequestDto;
import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service // Marca como um componente Spring (Serviço)
@RequiredArgsConstructor // Lombok: Injeta dependências via construtor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Injeta o codificador de senhas

    public User registerUser(RegisterRequestDto registerRequest) {
        // 1. Verificar se o username já existe
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Nome de usuário já existe: " + registerRequest.getUsername());
        }

        // 2. Verificar se o e-mail já existe
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já existe: " + registerRequest.getEmail());
        }

        // 3. Criar um novo usuário
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());

        // 4. *** CODIFICAR A SENHA ANTES DE SALVAR! ***
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // 5. Definir o papel padrão (pode ser mais complexo no futuro)
        newUser.setRole("ROLE_USER");

        // 6. Salvar o novo usuário no banco
        return userRepository.save(newUser);
    }

    // Você pode adicionar outros métodos aqui no futuro, como:
    // - findUserById(UUID id)
    // - updateUserProfile(User user)
    // - changePassword(User user, String oldPassword, String newPassword)
}