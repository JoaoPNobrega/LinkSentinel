// Local: src/main/java/br/cesar/school/linksentinel/view/LinkCheckerView.java
package br.cesar.school.linksentinel.view;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.service.LinkVerificationService;
import br.cesar.school.linksentinel.service.SecurityService;
import br.cesar.school.linksentinel.service.strategy.VerificationStrategyType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.format.DateTimeFormatter;

@Route(value = "checker", layout = MainLayout.class)
@PageTitle("Verificador | Link Sentinel")
@PermitAll
public class LinkCheckerView extends VerticalLayout {

    private final LinkVerificationService verificationService;
    private final SecurityService securityService;

    private final TextField urlField;
    private final Button checkButton;
    private final VerticalLayout resultContainer;

    public LinkCheckerView(LinkVerificationService verificationService, SecurityService securityService) {
        this.verificationService = verificationService;
        this.securityService = securityService;

        addClassName("checker-view");
        setSizeFull();
        setPadding(false);
        setAlignItems(Alignment.CENTER);

        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setWidthFull();
        contentWrapper.setMaxWidth("700px");
        contentWrapper.setAlignItems(Alignment.STRETCH);
        contentWrapper.getStyle().set("padding", LumoUtility.Padding.LARGE);

        H2 title = new H2("Verificador de Links");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.TextColor.HEADER);
        title.getStyle().set("text-align", "center");

        urlField = new TextField("Insira a URL para verificar:");
        urlField.setWidthFull();
        urlField.setPlaceholder("https://www.example.com");
        urlField.setClearButtonVisible(true);

        checkButton = new Button("Verificar Agora", VaadinIcon.SEARCH.create());
        checkButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        checkButton.addClickListener(e -> verifyLink());
        checkButton.addClickShortcut(Key.ENTER);
        checkButton.setWidthFull();
        checkButton.getStyle().set("margin-top", LumoUtility.Margin.Top.SMALL);

        VerticalLayout inputFieldLayout = new VerticalLayout(urlField, checkButton);
        inputFieldLayout.setPadding(false);
        inputFieldLayout.setSpacing(true);
        inputFieldLayout.setAlignItems(Alignment.STRETCH);

        resultContainer = new VerticalLayout();
        resultContainer.setWidthFull();
        resultContainer.setPadding(false);
        resultContainer.getStyle().set("margin-top", LumoUtility.Margin.Top.LARGE);
        resultContainer.addClassName("result-card-style"); 

        Div resultCard = new Div(resultContainer);
        resultCard.setWidthFull();
        resultCard.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", LumoUtility.Padding.LARGE)
                .set("box-shadow", "var(--lumo-box-shadow-s)");
        resultCard.setVisible(false); 

        contentWrapper.add(title, inputFieldLayout, resultCard);
        add(contentWrapper);
    }

    private void verifyLink() {
        String url = urlField.getValue();
        if (url == null || url.trim().isEmpty()) {
            Notification.show("Por favor, insira uma URL.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        if (!url.matches("^(?i)(https?|ftp)://.*")) {
            url = "http://" + url;
            urlField.setValue(url); 
        }

        UserDetails currentUser = securityService.getAuthenticatedUser();
        if (currentUser == null) {
            Notification.show("Erro: Usuário não autenticado.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        resultContainer.removeAll();
        resultContainer.getParent().ifPresent(parent -> ((Div)parent).setVisible(true)); 
        resultContainer.add(new Paragraph("Verificando... Por favor, aguarde."));
        checkButton.setEnabled(false);

        try {
            CheckResult result = verificationService.performCheck(
                    url,
                    currentUser.getUsername(),
                    VerificationStrategyType.REDIRECT_CHECK 
            );
            displayResults(result);
        } catch (Exception e) {
            Notification.show("Ocorreu um erro ao verificar: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            resultContainer.removeAll();
            resultContainer.add(new Paragraph("Falha na verificação. Tente novamente."));
        } finally {
            checkButton.setEnabled(true);
        }
    }

    private void displayResults(CheckResult result) {
        resultContainer.removeAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        H2 resultTitle = new H2("Resultado da Verificação");
        resultTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.MEDIUM);
        resultContainer.add(resultTitle);

        addResultRow("URL Original:", result.getLink().getUrl());
        addResultRow("Verificado em:", result.getCheckTimestamp().format(formatter));
        
        if (result.getUser() != null) {
            addResultRow("Verificado por:", result.getUser().getUsername());
        } else {
            addResultRow("Verificado por:", "N/A (usuário não associado)");
        }


        Span statusSpan = new Span();

        if (result.isAccessible()) {
            statusSpan.setText("Acessível (HTTP " + result.getStatusCode() + ")");
            statusSpan.getElement().getThemeList().set("badge success pill", true);
        } else {
            statusSpan.setText("Inacessível (HTTP " + result.getStatusCode() + ")");

            statusSpan.getElement().getThemeList().set("badge error pill", true);
        }
        addResultRow("Status:", statusSpan);


        if (result.getFinalUrl() != null && !result.getFinalUrl().equals(result.getLink().getUrl())) {
            addResultRow("URL Final (após redirects):", result.getFinalUrl());
        }

        if (result.getFailureReason() != null && !result.getFailureReason().isBlank()) {
            Span errorSpan = new Span("Detalhe da Falha: " + result.getFailureReason());
            errorSpan.getElement().getThemeList().set("badge error pill", true);
            addResultRow("Falha:", errorSpan);
        }
    }

    private void addResultRow(String label, String value) {
        if (value == null || value.isBlank()) return;
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        Span labelSpan = new Span(label);
        labelSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.SEMIBOLD);
        labelSpan.getStyle().set("margin-right", LumoUtility.Margin.Right.SMALL);
        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("word-break", "break-all"); 
        row.add(labelSpan, valueSpan);
        row.setFlexGrow(1, valueSpan); 
        resultContainer.add(row);
    }

    private void addResultRow(String label, Component valueComponent) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        Span labelSpan = new Span(label);
        labelSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.SEMIBOLD);
        labelSpan.getStyle().set("margin-right", LumoUtility.Margin.Right.SMALL);
        row.add(labelSpan, valueComponent);
        row.setFlexGrow(1, valueComponent);
        row.setAlignItems(Alignment.BASELINE);
        resultContainer.add(row);
    }
}