package br.cesar.school.linksentinel.bdd.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given; 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when; 
import org.springframework.boot.test.mock.mockito.MockBean;
import br.cesar.school.linksentinel.model.User; 
import br.cesar.school.linksentinel.repository.UserRepository; 
import br.cesar.school.linksentinel.service.SecurityService; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional; 
import java.util.UUID; 

public class CommonSteps {

    public static String sharedDisplayedMessage;
    public static User sharedAuthenticatedUser;

    @MockBean
    private UserRepository userRepositoryCommon; 
    @MockBean
    private BCryptPasswordEncoder passwordEncoderCommon; 
    @Autowired
    private SecurityService securityServiceCommon;

    @Before
    public void setupScenario() {
        reset(userRepositoryCommon, passwordEncoderCommon, securityServiceCommon);
        
        sharedDisplayedMessage = null;
        sharedAuthenticatedUser = null;
    }

    @Given("que eu estou logado como {string}")
    public void queEuEstouLogadoComo(String username) {
        User user = new User();
        user.setUsername(username);
        user.setId(UUID.randomUUID());
        user.setEmail(username + "@example.com"); 
        user.setRole("ROLE_USER"); 
        user.setPassword("hashed_password_for_" + username);

        when(userRepositoryCommon.findByUsername(username)).thenReturn(Optional.of(user));
        when(securityServiceCommon.getAuthenticatedUser()).thenReturn(user);

        sharedAuthenticatedUser = user;
    }

    @And("uma mensagem de erro deve ser exibida {string}")
    public void umaMensagemDeErroDeveSerExibida(String expectedErrorMessage) {
        assertNotNull("A mensagem de erro não foi definida no passo anterior.", sharedDisplayedMessage);
        assertEquals(expectedErrorMessage, sharedDisplayedMessage);
    }
}