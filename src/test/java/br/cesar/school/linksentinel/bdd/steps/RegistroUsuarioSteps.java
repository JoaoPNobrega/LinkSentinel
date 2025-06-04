package br.cesar.school.linksentinel.bdd.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import org.mockito.Mockito;

import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.UserRepository;
import br.cesar.school.linksentinel.service.UserService;
import br.cesar.school.linksentinel.dto.RegisterRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

public class RegistroUsuarioSteps {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    private boolean registroBemSucedido;

    @Before
    public void setup() {
        reset(userRepository, passwordEncoder);
        registroBemSucedido = false;
        CommonSteps.sharedDisplayedMessage = null;
        CommonSteps.sharedAuthenticatedUser = null; 
    }

    @Given("que eu não estou registrado no sistema")
    public void queEuNaoEstouRegistradoNoSistema() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPasswordMock");
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getId() == null) {
                user.setId(UUID.randomUUID());
            }
            return user;
        });
    }

    @Given("que um usuário com o nome de usuário {string} já está registrado")
    public void queUmUsuarioComONomeDeUsuarioJaEstaRegistrado(String username) {
        User existingUser = new User();
        existingUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
    }

    @Given("que um usuário com o email {string} já está registrado")
    public void queUmUsuarioComOEmailJaEstaRegistrado(String email) {
        User existingUser = new User();
        existingUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
    }

    @When("eu preencho o formulário de registro com nome de usuário {string}, email {string} e senha {string}")
    public void euPreenchoOFormularioDeRegistroComNomeDeUsuarioEmailESenha(String username, String email, String password) {
        RegisterRequestDto request = new RegisterRequestDto(username, email, password);

        try {
            userService.registerUser(request);
            registroBemSucedido = true;
            CommonSteps.sharedDisplayedMessage = "Registro realizado com sucesso. Faça login.";
        } catch (IllegalArgumentException e) {
            registroBemSucedido = false;
            CommonSteps.sharedDisplayedMessage = e.getMessage();
        } catch (Exception e) {
            registroBemSucedido = false;
            CommonSteps.sharedDisplayedMessage = "Ocorreu um erro inesperado durante o registro.";
        }
    }

    @And("eu submeto o formulário de registro")
    public void euSubmetoOFormularioDeRegistro() {

    }

    @Then("eu devo ser redirecionado para a página de login")
    public void euDevoSerRedirecionadoParaAPaginaDeLogin() {
        assertTrue("Não foi redirecionado para a página de login.", registroBemSucedido);
    }

    @And("uma mensagem de sucesso deve ser exibida {string}")
    public void umaMensagemDeSucessoDeveSerExibida(String expectedMessage) {
        assertEquals(expectedMessage, CommonSteps.sharedDisplayedMessage);
    }

    @Then("o registro deve falhar")
    public void oRegistroDeveFalhar() {
        assertTrue("O registro não falhou como esperado.", !registroBemSucedido);
        assertNotNull("Mensagem de erro não foi exibida.", CommonSteps.sharedDisplayedMessage);
    }

}