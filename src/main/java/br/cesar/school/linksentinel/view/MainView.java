// Local: src/main/java/br/cesar/school/linksentinel/view/MainView.java
package br.cesar.school.linksentinel.view;

import br.cesar.school.linksentinel.model.User;
import br.cesar.school.linksentinel.repository.CheckResultRepository;
import br.cesar.school.linksentinel.repository.LinkRepository;
import br.cesar.school.linksentinel.repository.UserRepository;
import br.cesar.school.linksentinel.service.SecurityService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard | Link Sentinel")
@PermitAll
public class MainView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final CheckResultRepository checkResultRepository;
    private final LinkRepository linkRepository;

    public MainView(SecurityService securityService,
                    UserRepository userRepository,
                    CheckResultRepository checkResultRepository,
                    LinkRepository linkRepository) {
        this.securityService = securityService;
        this.userRepository = userRepository;
        this.checkResultRepository = checkResultRepository;
        this.linkRepository = linkRepository;

        addClassName("dashboard-view");
        setSizeFull();
        setPadding(true);
        setAlignItems(Alignment.CENTER); 

        UserDetails userDetails = securityService.getAuthenticatedUser();
        String username = userDetails != null ? userDetails.getUsername() : "Usuário";

        H1 welcomeTitle = new H1("Bem-vindo(a), " + username + "!");
        welcomeTitle.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.TextColor.HEADER);
        welcomeTitle.getStyle().set("text-align", "center");


        Paragraph subtitle = new Paragraph("Aqui está um resumo da sua atividade no Link Sentinel.");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Bottom.XLARGE);
        subtitle.getStyle().set("text-align", "center");


        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.addClassName("stats-layout");
        statsLayout.setSpacing(true);
        statsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        statsLayout.setWidthFull();
        statsLayout.getStyle().set("flex-wrap", "wrap"); 
        statsLayout.getStyle().set("gap", LumoUtility.Gap.LARGE); // Espaço entre os cards


        long linksVerificadosCount = 0;
        if (userDetails != null) {
            User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (currentUser != null) {
                linksVerificadosCount = checkResultRepository.countByUser(currentUser);
            }
        }
        long linksMonitoradosCount = linkRepository.countByMonitoredTrue();

        statsLayout.add(
                createStatCard("Links Verificados", String.valueOf(linksVerificadosCount), VaadinIcon.CHECK_SQUARE_O, "Total de links que você já submeteu para verificação."),
                createStatCard("Links Monitorados", String.valueOf(linksMonitoradosCount), VaadinIcon.EYE, "Total de links atualmente marcados para monitoramento no sistema."),
                createStatCard("Ameaças Detetadas", "0", VaadinIcon.SHIELD, "Total de ameaças encontradas recentemente (Safe Browse / IA). (Em breve)")
        );

        H3 actionsTitle = new H3("Ações Rápidas");
        actionsTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Top.XLARGE, LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.TextColor.HEADER);
        actionsTitle.getStyle().set("text-align", "center");

        Button verifyButton = new Button("Verificar Novo Link", VaadinIcon.SEARCH.create(),
                e -> UI.getCurrent().navigate("checker"));
        verifyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        verifyButton.getStyle().set("cursor", "pointer");


        Button historyButton = new Button("Ver Histórico Completo", VaadinIcon.ARCHIVES.create(),
                e -> UI.getCurrent().navigate("history"));
        historyButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
        historyButton.getStyle().set("cursor", "pointer");
        
        HorizontalLayout actionsLayout = new HorizontalLayout(verifyButton, historyButton);
        actionsLayout.setSpacing(true);
        actionsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        actionsLayout.setWidthFull();

        // Adicionar tudo ao layout principal do dashboard
        // Para garantir que o conteúdo não ocupe a tela inteira e fique centralizado:
        VerticalLayout contentWrapper = new VerticalLayout(welcomeTitle, subtitle, statsLayout, actionsTitle, actionsLayout);
        contentWrapper.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        contentWrapper.setMaxWidth("900px"); // Largura máxima para o conteúdo do dashboard
        contentWrapper.getStyle().set("margin", "0 auto"); // Centraliza o wrapper

        add(contentWrapper);
    }

    private VerticalLayout createStatCard(String title, String value, VaadinIcon iconEnum, String description) {
        Icon icon = iconEnum.create();
        icon.setSize("32px"); // Ícone um pouco maior
        icon.addClassName(LumoUtility.TextColor.PRIMARY);
        // icon.getStyle().set("margin-bottom", LumoUtility.Gap.SMALL); // Movido para o cardInfo

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.HEADER); // Valor mais destacado

        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.TextColor.BODY); // Título do card

        Paragraph descParagraph = new Paragraph(description);
        descParagraph.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);
        descParagraph.getStyle().set("text-align", "center");
        descParagraph.getStyle().set("margin-top", LumoUtility.Gap.SMALL);


        VerticalLayout cardInfo = new VerticalLayout(icon, valueSpan, titleSpan, descParagraph);
        cardInfo.setAlignItems(Alignment.CENTER); 
        cardInfo.setPadding(false);
        cardInfo.setSpacing(true); // Adiciona espaçamento interno
        cardInfo.getStyle().set("line-height", "1.3");


        VerticalLayout card = new VerticalLayout(cardInfo);
        card.setAlignItems(Alignment.CENTER); // Card centraliza seu conteúdo
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.Horizontal.LARGE, // Padding horizontal
                LumoUtility.Padding.Vertical.MEDIUM,  // Padding vertical
                LumoUtility.BoxShadow.MEDIUM 
        );
        card.setWidth("250px"); 
        card.setHeight("200px");
        card.setJustifyContentMode(JustifyContentMode.CENTER); // Centraliza verticalmente o conteúdo do card
        card.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)"); // Borda sutil

        return card;
    }
}