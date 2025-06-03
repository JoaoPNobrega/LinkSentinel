package br.cesar.school.linksentinel.view;

import br.cesar.school.linksentinel.dto.RegisterRequestDto;
import br.cesar.school.linksentinel.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;

import lombok.extern.slf4j.Slf4j;

@Route("register")
@PageTitle("Registrar | Link Sentinel")
@AnonymousAllowed
@Slf4j
public class RegisterView extends VerticalLayout {

    private final UserService userService;
    private Binder<RegisterRequestDto> binder = new BeanValidationBinder<>(RegisterRequestDto.class);

    public RegisterView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("register-view");
        getStyle().set("padding", "20px");

        VerticalLayout formContentLayout = new VerticalLayout();
        formContentLayout.setAlignItems(Alignment.CENTER);
        formContentLayout.setSpacing(true);
        formContentLayout.getStyle().set("padding", "var(--lumo-space-l)");
        formContentLayout.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        formContentLayout.getStyle().set("box-shadow", "var(--lumo-box-shadow-s)");
        formContentLayout.getStyle().set("background-color", "var(--lumo-base-color)");
        formContentLayout.setMaxWidth("420px");

        H2 title = new H2("Crie sua Conta");
        title.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        TextField usernameField = new TextField("Nome de Usuário");
        usernameField.setWidthFull();
        EmailField emailField = new EmailField("E-mail");
        emailField.setWidthFull();
        PasswordField passwordField = new PasswordField("Senha");
        passwordField.setWidthFull();
        PasswordField confirmPasswordField = new PasswordField("Confirmar Senha");
        confirmPasswordField.setWidthFull();

        binder.bind(usernameField, "username");
        binder.bind(emailField, "email");
        binder.bind(passwordField, "password");

        FormLayout formLayout = new FormLayout(
                usernameField,
                emailField,
                passwordField,
                confirmPasswordField
        );

        Button registerButton = new Button("Registrar", e -> registerUser(confirmPasswordField.getValue()));
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();

        RouterLink loginLink = new RouterLink("Já tem uma conta? Faça login", LoginView.class);
        loginLink.getStyle().set("font-size", "var(--lumo-font-size-s)");

        formContentLayout.add(title, formLayout, registerButton, loginLink);
        add(formContentLayout);
    }

    private void registerUser(String confirmPassword) {
        RegisterRequestDto registerRequest = new RegisterRequestDto();
        try {
            binder.writeBean(registerRequest);
        } catch (ValidationException e) {
            Notification.show("Por favor, corrija os erros no formulário.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!registerRequest.getPassword().equals(confirmPassword)) {
            Notification.show("As senhas não coincidem.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            userService.registerUser(registerRequest);
            Notification.show("Usuário registrado com sucesso! Você pode fazer login agora.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate("login");
        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Ocorreu um erro inesperado durante o registro.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            log.error("Erro no registro: ", e);
        }
    }
}