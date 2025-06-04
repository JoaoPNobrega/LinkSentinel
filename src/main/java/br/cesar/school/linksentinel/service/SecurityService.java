package br.cesar.school.linksentinel.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SecurityService {

    public UserDetails getAuthenticatedUser() {
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
     * 
     * 
     */
    public void logout() {
        log.info("SecurityService: Logout acionado. A navegação para /logout é feita pelo componente Anchor na UI.");

    }
}