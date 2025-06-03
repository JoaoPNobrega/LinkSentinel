package br.cesar.school.linksentinel.view;

import br.cesar.school.linksentinel.model.CheckResult;
import br.cesar.school.linksentinel.model.Link;
import br.cesar.school.linksentinel.service.HistoryService;
import br.cesar.school.linksentinel.service.LinkService;
import br.cesar.school.linksentinel.service.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
// Removido Span não utilizado
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.TextRenderer; // Adicionado para formatação do Uptime
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "history", layout = MainLayout.class)
@PageTitle("Histórico | Link Sentinel")
@PermitAll
public class HistoryView extends VerticalLayout {

    private final HistoryService historyService;
    private final SecurityService securityService;
    private final LinkService linkService;

    private Grid<CheckResult> grid = new Grid<>(CheckResult.class, false);
    private UserDetails currentUserDetails;
    private Paragraph noHistoryMessage;

    public HistoryView(HistoryService historyService, SecurityService securityService, LinkService linkService) {
        this.historyService = historyService;
        this.securityService = securityService;
        this.linkService = linkService;
        this.currentUserDetails = securityService.getAuthenticatedUser();

        addClassName("history-view");
        setSizeFull();
        setPadding(true);

        H2 title = new H2("Seu Histórico de Verificações");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);

        Button clearHistoryButton = new Button("Limpar Todo o Histórico", VaadinIcon.TRASH.create());
        clearHistoryButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        clearHistoryButton.addClickListener(e -> confirmClearAllHistory());
        clearHistoryButton.getStyle().set("margin-left", "auto");

        HorizontalLayout headerLayout = new HorizontalLayout(title, clearHistoryButton);
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.BASELINE);

        noHistoryMessage = new Paragraph("Nenhum histórico de verificação encontrado.");
        noHistoryMessage.setVisible(false);

        configureGrid();
        add(headerLayout, grid, noHistoryMessage);
        updateList();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

        grid.addColumn(result -> result.getCheckTimestamp().format(formatter))
                .setHeader("Data/Hora").setSortable(true).setFlexGrow(0).setWidth("150px");
        grid.addColumn(result -> result.getLink().getUrl())
                .setHeader("URL Original").setSortable(true).setFlexGrow(2);

        // Coluna de Uptime do Link
        grid.addColumn(new TextRenderer<>(checkResult -> {
                    Link link = checkResult.getLink();
                    if (link != null) {
                        return String.format("%.2f%%", historyService.calculateUptimePercentage(link));
                    }
                    return "N/A";
                }))
                .setHeader("Uptime Link (%)")
                .setKey("linkUptime")
                .setSortable(false) // Ordenar isso pode ser complexo/caro aqui; pode-se implementar comparador se necessário
                .setFlexGrow(0).setWidth("140px");

        grid.addColumn(CheckResult::getStatusCode)
                .setHeader("Status HTTP").setSortable(true).setFlexGrow(0).setWidth("120px"); // Nome do header alterado para clareza
        grid.addColumn(result -> result.isAccessible() ? "Sim" : "Não")
                .setHeader("Acessível?").setSortable(true).setFlexGrow(0).setWidth("120px");
        grid.addColumn(CheckResult::getFinalUrl)
                .setHeader("URL Final").setSortable(true).setFlexGrow(1);

        grid.addComponentColumn(checkResult -> {
            Link link = checkResult.getLink();
            Checkbox monitoredCheckbox = new Checkbox(link.isMonitored());
            monitoredCheckbox.addValueChangeListener(event -> {
                try {
                    linkService.toggleLinkMonitoring(link.getId());
                    link.setMonitored(event.getValue()); // Optimistic update
                    Notification.show("Monitoramento de '" + সংক্ষিপ্তUrl(link.getUrl()) + "' alterado.",
                                    2000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception e) {
                    Notification.show("Erro: " + e.getMessage(), 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    monitoredCheckbox.setValue(link.isMonitored()); // Revert on error
                }
            });
            return monitoredCheckbox;
        }).setHeader("Monitorar?").setFlexGrow(0).setWidth("130px");

        grid.addComponentColumn(checkResult -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.setAriaLabel("Excluir verificação " + checkResult.getLink().getUrl());
            deleteButton.addClickListener(e -> confirmDeleteSingleResult(checkResult));
            return deleteButton;
        }).setHeader("Ações").setFlexGrow(0).setWidth("100px");

        grid.getColumns().forEach(col -> col.setResizable(true));
    }
    
    private String সংক্ষিপ্তUrl(String url) {
        if (url == null) return "";
        return url.length() > 30 ? url.substring(0, 27) + "..." : url;
    }

    private void confirmDeleteSingleResult(CheckResult checkResult) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Excluir Verificação");
        dialog.setText("Tem certeza que deseja excluir o registro de verificação para: " +
                checkResult.getLink().getUrl() + " de " +
                checkResult.getCheckTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")) + "?");
        
        dialog.setConfirmText("Excluir");
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelText("Cancelar");
        dialog.setCancelable(true);

        dialog.addConfirmListener(event -> {
            try {
                historyService.deleteCheckResult(checkResult.getId()); 
                Notification.show("Registro excluído.", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateList();
            } catch (Exception e) {
                Notification.show("Erro ao excluir: " + e.getMessage(), 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }

    private void confirmClearAllHistory() {
        if (this.currentUserDetails == null) return;

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Limpar Todo o Histórico?");
        dialog.setText("Tem certeza que deseja excluir TODOS os seus registros de verificação? Esta ação não pode ser desfeita.");

        dialog.setConfirmText("Sim, Excluir Tudo");
        dialog.setConfirmButtonTheme("error primary");
        dialog.setCancelText("Cancelar");
        dialog.setCancelable(true);

        dialog.addConfirmListener(event -> {
            try {
                historyService.deleteAllHistoryForUser(this.currentUserDetails.getUsername());
                Notification.show("Todo o histórico foi excluído.", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateList();
            } catch (Exception e) {
                Notification.show("Erro ao limpar histórico: " + e.getMessage(), 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }

    private void updateList() {
        if (this.currentUserDetails != null) {
            List<CheckResult> results = historyService.getHistoryForUser(this.currentUserDetails.getUsername());
            grid.setItems(results);
            
            boolean hasResults = !results.isEmpty();
            grid.setVisible(hasResults);
            noHistoryMessage.setVisible(!hasResults);
        } else {
            grid.setItems(List.of());
            grid.setVisible(false);
            noHistoryMessage.setVisible(true);
        }
    }
}