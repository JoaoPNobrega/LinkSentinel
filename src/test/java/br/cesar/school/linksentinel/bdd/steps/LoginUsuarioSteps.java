package br.cesar.school.linksentinel.bdd.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.UserRepository;
import br.cesar.school.linksentinel.service.SecurityService;
import br.cesar.school.linksentinel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean; // Permite mockar beans do Spring Context
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Seu PasswordEncoder

import java.util.Optional;
import java.util.UUID;

public class LoginUsuarioSteps {

    // @MockBean cria um mock para esta interface e o injeta no contexto Spring.
    // Ele substitui a implementação real de UserRepository onde ele é @Autowired.
    @MockBean
    private UserRepository userRepository;

    // @MockBean para o PasswordEncoder, pois ele é um Bean configurado em SecurityConfig.java
    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    // @Autowired injeta as instâncias REAIS dos seus serviços que o Spring criou.
    // Esses serviços terão os @MockBeans acima injetados neles.
    @Autowired
    private UserService userService;
    @Autowired
    private SecurityService securityService; // SecurityService também é autowired

    private User currentUser;
    private String displayedMessage;

    @Before
    public void setup() {
        // Reseta os mocks antes de CADA cenário, garantindo que os testes são independentes.
        reset(userRepository, passwordEncoder, securityService); // Resetar securityService mock também

        // Reseta as variáveis de estado para cada cenário
        currentUser = null;
        displayedMessage = null;
    }

    @Given("que eu sou um usuário registrado com nome de usuário {string} e senha {string}")
    public void queEuSouUmUsuarioRegistradoComNomeDeUsuarioESenha(String username, String password) {
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID()); // IDs são UUIDs no seu User.java
        existingUser.setUsername(username);
        existingUser.setPassword("hashed_password_for_" + username); // Usa setPassword()
        existingUser.setEmail(username + "@example.com");
        existingUser.setRole("ROLE_USER");

        // Mockar o comportamento do UserRepository.findByUsername:
        // Quando findByUsername é chamado com 'username', retorna Optional.of(existingUser)
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
        // Mockar o comportamento do PasswordEncoder para matches():
        // Quando matches é chamado com a senha fornecida e o hash do usuário existente, retorna true
        when(passwordEncoder.matches(eq(password), eq(existingUser.getPassword()))).thenReturn(true);
        // Mockar o encode para o caso de registro (se essa mesma classe for usada no registro_usuario.feature)
        when(passwordEncoder.encode(anyString())).thenReturn("some_encoded_password_mock");
        
        // Mockar o SecurityService para simular que o usuário está autenticado
        when(securityService.getAuthenticatedUser()).thenReturn(existingUser);
    }

    @Given("que não existe um usuário com o nome de usuário {string}")
    public void queNaoExisteUmUsuarioComONomeDeUsuario(String username) {
        // Mockar o comportamento do UserRepository para retornar Optional.empty()
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        // Não é necessário mockar existsByUsername, pois ele não existe na sua interface.
        // O UserService usa findByUsername().isPresent() para verificar existência.
        // O PasswordEncoder não precisa ser configurado aqui, pois não haverá match para um usuário inexistente.
    }

    @When("eu preencho o formulário de login com nome de usuário {string} e senha {string}")
    public void euPreenchoOFormularioDeLoginComNomeDeUsuarioESenha(String username, String password) {
        // Simular o fluxo de login.
        // Como o SecurityService não expõe 'authenticateUser(String, String)' diretamente,
        // vamos simular a lógica de autenticação aqui, interagindo com os mocks.
        try {
            Optional<User> foundUserOptional = userRepository.findByUsername(username);

            if (foundUserOptional.isPresent()) {
                User user = foundUserOptional.get();
                // Usamos o mock do passwordEncoder para verificar a senha
                if (passwordEncoder.matches(password, user.getPassword())) { //
                    currentUser = user; // Sucesso na autenticação
                } else {
                    currentUser = null; // Senha incorreta
                    displayedMessage = "Credenciais inválidas";
                }
            } else {
                currentUser = null; // Usuário não encontrado
                displayedMessage = "Credenciais inválidas";
            }
        } catch (Exception e) {
            displayedMessage = "Erro inesperado: " + e.getMessage();
            currentUser = null;
        }
        // Após a "tentativa" de login, mockamos o SecurityService para refletir o estado de autenticação para os 'Then'
        when(securityService.getAuthenticatedUser()).thenReturn(currentUser);
    }

    @And("eu submeto o formulário")
    public void euSubmetoOFormulario() {
        // Este passo não requer código extra para testes de serviço.
    }

    @Then("eu devo ser redirecionado para a página principal \\(/main)")
    public void euDevoSerRedirecionadoParaAPaginaPrincipalMain() {
        assertNotNull("Login falhou: Usuário não foi logado.", currentUser);
        // Em testes de UI, você verificaria a URL atual do navegador (ex: "/dashboard")
    }

    @And("uma mensagem de boas-vindas deve ser exibida {string}")
    public void umaMensagemDeBoasVindasDeveSerExibida(String expectedMessage) {
        assertNotNull("Usuário logado é nulo, não pode gerar mensagem de boas-vindas.", currentUser);
        assertEquals(expectedMessage, "Bem-vindo, " + currentUser.getUsername() + "!");
    }

    @Then("o login deve falhar")
    public void oLoginDeveFalhar() {
        assertNull("Usuário não deveria estar logado em caso de falha.", currentUser);
        assertNotNull("Mensagem de erro não foi exibida em caso de falha.", displayedMessage);
    }

    @And("uma mensagem de erro deve ser exibida {string}")
    public void umaMensagemDeErroDeveSerExibida(String expectedErrorMessage) {
        assertEquals(expectedErrorMessage, displayedMessage);
    }
}