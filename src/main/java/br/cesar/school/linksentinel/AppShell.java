// Local: src/main/java/br/cesar/school/linksentinel/AppShell.java
package br.cesar.school.linksentinel;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo; // Importar Lumo

@Theme(themeClass = Lumo.class, variant = Lumo.DARK) // Diz explicitamente para usar Lumo com variante DARK
public class AppShell implements AppShellConfigurator {
    // A classe pode continuar vazia
}