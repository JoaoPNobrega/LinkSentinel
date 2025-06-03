package br.cesar.school.linksentinel.view;

import br.cesar.school.linksentinel.service.SecurityService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor; // <-- IMPORTANTE: Usaremos Anchor
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.userdetails.UserDetails;

public class MainLayout extends AppLayout {

    private final SecurityService securityService; // Usado para pegar o nome do usuário

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("LinkSentinel");
        logo.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.NONE);

        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.setAriaLabel("Menu toggle");

        UserDetails userDetails = securityService.getAuthenticatedUser();
        Span usernameSpan = new Span();
        if (userDetails != null) {
            usernameSpan.setText(userDetails.getUsername());
            usernameSpan.addClassNames(
                    LumoUtility.TextColor.BODY,
                    LumoUtility.FontSize.SMALL,
                    LumoUtility.Margin.Right.MEDIUM,
                    LumoUtility.FontWeight.SEMIBOLD
            );
        }

        // *** MUDANÇA AQUI: Usando Anchor para o Logout e redirecionando para /login ***
        Anchor logoutLink = new Anchor("/login", "Sair"); // Alterado o href para "/login"
        logoutLink.getElement().setAttribute("router-ignore", ""); // Adicionado para que o Vaadin não tente rotear internamente

        Icon logoutIcon = VaadinIcon.SIGN_OUT.create();
        logoutIcon.getStyle().set("margin-right", LumoUtility.Margin.Right.XSMALL);
        logoutLink.getElement().insertChild(0, logoutIcon.getElement()); // Adiciona ícone antes do texto "Sair"

        // Estilizando o Anchor para parecer um botão sutil
        logoutLink.getStyle()
            .set("text-decoration", "none")
            .set("color", "var(--lumo-tertiary-text-color)") // Cor mais sutil
            .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("font-weight", "500")
            .set("line-height", "1"); // Para alinhar melhor com outros botões/texto
        logoutLink.getElement().getStyle().set("cursor", "pointer");

        // Efeito hover simples para o Anchor
        logoutLink.getElement().addEventListener("mouseover", e -> logoutLink.getStyle().set("background-color", "var(--lumo-contrast-5pct)"));
        logoutLink.getElement().addEventListener("mouseout", e -> logoutLink.getStyle().remove("background-color"));
        // *** FIM DA MUDANÇA PARA ANCHOR ***

        HorizontalLayout headerRight = new HorizontalLayout(usernameSpan, logoutLink);
        headerRight.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout header = new HorizontalLayout(drawerToggle, logo, headerRight);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.BoxShadow.SMALL
        );

        addToNavbar(true, header);
    }

    private void createDrawer() {
        RouterLink dashboardLink = createMenuLink("Dashboard", VaadinIcon.HOME_O, MainView.class);
        RouterLink checkerLink = createMenuLink("Verificar Links", VaadinIcon.SEARCH_PLUS, LinkCheckerView.class);
        RouterLink historyLink = createMenuLink("Histórico", VaadinIcon.ARCHIVES, HistoryView.class);

        VerticalLayout menuLayout = new VerticalLayout(dashboardLink, checkerLink, historyLink);
        menuLayout.setPadding(false);
        menuLayout.setSpacing(true);
        menuLayout.addClassName(LumoUtility.Padding.Vertical.MEDIUM);
        menuLayout.setHeightFull();

        addToDrawer(menuLayout);
    }

    private RouterLink createMenuLink(String text, VaadinIcon icon, Class<? extends com.vaadin.flow.component.Component> navigationTarget) {
        RouterLink link = new RouterLink(navigationTarget);
        link.addClassName("menu-link");

        Icon linkIcon = icon.create();
        linkIcon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.SECONDARY);
        linkIcon.getStyle().set("margin-right", LumoUtility.Margin.Right.MEDIUM);

        Span linkText = new Span(text);
        linkText.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.BODY);
        linkText.getStyle().set("font-weight", "500");

        HorizontalLayout linkLayout = new HorizontalLayout(linkIcon, linkText);
        linkLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        linkLayout.getStyle().set("padding", LumoUtility.Padding.Vertical.MEDIUM + " " + LumoUtility.Padding.Horizontal.MEDIUM);
        linkLayout.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        linkLayout.getStyle().set("transition", "background-color 0.1s ease-in-out, color 0.1s ease-in-out");

        link.getElement().addEventListener("mouseover", e -> {
            linkLayout.getStyle().set("background-color", "var(--lumo-contrast-10pct)");
        });
        link.getElement().addEventListener("mouseout", e -> {
            linkLayout.getStyle().remove("background-color");
        });

        link.add(linkLayout);
        return link;
    }
}