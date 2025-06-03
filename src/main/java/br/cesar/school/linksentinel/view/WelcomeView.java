// Local: src/main/java/br/cesar/school/linksentinel/view/WelcomeView.java
package br.cesar.school.linksentinel.view;

import br.cesar.school.linksentinel.service.SecurityService; // Importar o SecurityService
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.userdetails.UserDetails; 

@Route("") 
@PageTitle("Bem-vindo | Link Sentinel")
@AnonymousAllowed 
public class WelcomeView extends VerticalLayout implements BeforeEnterObserver {

    private final SecurityService securityService;

    public WelcomeView(SecurityService securityService) {
        this.securityService = securityService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("welcome-view");
        getStyle().set("padding", "20px");


        H1 title = new H1("LINKSENTINEL");
        title.getStyle().set("font-size", "4em").set("font-weight", "bold");

        Paragraph subtitle = new Paragraph("Seu verificador inteligente de links e segurança.");
        subtitle.getStyle().set("font-size", "1.2em").set("color", "var(--lumo-secondary-text-color)");

        Button loginButton = new Button("Login", e ->
                UI.getCurrent().navigate("login")
        );
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.getStyle().set("min-width", "150px");


        Button registerButton = new Button("Registrar", e ->
                UI.getCurrent().navigate("register")
        );
        registerButton.getStyle().set("min-width", "150px");

        add(title, subtitle, loginButton, registerButton);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        UserDetails authenticatedUser = securityService.getAuthenticatedUser();
        if (authenticatedUser != null) {
            event.forwardTo("dashboard");
        }
    }
}