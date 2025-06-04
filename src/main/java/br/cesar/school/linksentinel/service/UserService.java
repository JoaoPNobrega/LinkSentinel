package br.cesar.school.linksentinel.service;

import br.cesar.school.linksentinel.dto.RegisterRequestDto;
import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service 
@RequiredArgsConstructor 
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequestDto registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Nome de usuário já existe: " + registerRequest.getUsername());
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já existe: " + registerRequest.getEmail());
        }

        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());

        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        newUser.setRole("ROLE_USER");

        return userRepository.save(newUser);
    }

}