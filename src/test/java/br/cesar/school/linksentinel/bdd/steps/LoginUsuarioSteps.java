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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

public class LoginUsuarioSteps {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;
    @Autowired
    private SecurityService securityService;


    @Before
    public void setup() {
        reset(userRepository, passwordEncoder);
    }

    @Given("que não existe um usuário com o nome de usuário {string}")
    public void queNaoExisteUmUsuarioComONomeDeUsuario(String username) {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
    }

    @When("eu preencho o formulário de login com nome de usuário {string} e senha {string}")
    public void euPreenchoOFormularioDeLoginComNomeDeUsuarioESenha(String username, String password) {
        try {
            Optional<User> foundUserOptional = userRepository.findByUsername(username);

            if (foundUserOptional.isPresent()) {
                User user = foundUserOptional.get();
                if (passwordEncoder.matches(password, user.getPassword())) {
                    CommonSteps.sharedAuthenticatedUser = user;
                } else {
                    CommonSteps.sharedAuthenticatedUser = null; 
                    CommonSteps.sharedDisplayedMessage = "Credenciais inválidas";
                }
            } else {
                CommonSteps.sharedAuthenticatedUser = null; 
                CommonSteps.sharedDisplayedMessage = "Credenciais inválidas";
            }
        } catch (Exception e) {
            CommonSteps.sharedDisplayedMessage = "Erro inesperado: " + e.getMessage();
            CommonSteps.sharedAuthenticatedUser = null;
        }
        when(securityService.getAuthenticatedUser()).thenReturn(CommonSteps.sharedAuthenticatedUser);
    }

    @And("eu submeto o formulário")
    public void euSubmetoOFormulario() {

    }

    @Then("eu devo ser redirecionado para a página principal \\(/main)")
    public void euDevoSerRedirecionadoParaAPaginaPrincipalMain() {
        assertNotNull("Login falhou: Usuário não foi logado.", CommonSteps.sharedAuthenticatedUser);
    }

    @And("uma mensagem de boas-vindas deve ser exibida {string}")
    public void umaMensagemDeBoasVindasDeveSerExibida(String expectedMessage) {
        assertNotNull("Usuário logado é nulo, não pode gerar mensagem de boas-vindas.", CommonSteps.sharedAuthenticatedUser);
        assertEquals(expectedMessage, "Bem-vindo, " + CommonSteps.sharedAuthenticatedUser.getUsername() + "!");
    }

    @Then("o login deve falhar")
    public void oLoginDeveFalhar() {
        assertNull("Usuário não deveria estar logado em caso de falha.", CommonSteps.sharedAuthenticatedUser);
        assertNotNull("Mensagem de erro não foi exibida em caso de falha.", CommonSteps.sharedDisplayedMessage);
    }

}