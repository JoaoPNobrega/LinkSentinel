// Local: src/main/java/br/cesar/school/linksentinel/service/SecurityService.java
package br.cesar.school.linksentinel.service;

// com.vaadin.flow.component.UI; // Não é mais necessário para o logout aqui
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SecurityService {

    public UserDetails getAuthenticatedUser() {
        // ... (código existente) ...
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
            !(authentication.getPrincipal() instanceof String &&
              "anonymousUser".equals(authentication.getPrincipal().toString()))) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return (UserDetails) principal;
            }
        }
        return null;
    }

    /**
     * Este método pode ser usado para lógica de logout adicional no futuro,
     * mas a navegação para /logout é feita pelo Anchor no MainLayout.
     */
    public void logout() {
        log.info("SecurityService: Logout acionado. A navegação para /logout é feita pelo componente Anchor na UI.");
        // Nenhuma ação de UI.getCurrent().getPage().setLocation() aqui.
        // A URL /logout será tratada pelo Spring Security.
    }
}