package org.vaadin.example.auth.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.vaadin.example.shared.enums.UserTypes;

@Route("")
@PermitAll
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String role = auth.getAuthorities().iterator().next().getAuthority();
            
            // Kullanıcının rolüne göre ilgili sayfaya yönlendir
            if (role.equals(UserTypes.MUSTERI.getSpringRole())) {
                event.forwardTo("customer_main");
            } else if (role.equals(UserTypes.YAZILIMCI.getSpringRole())) {
                event.forwardTo("developer_main");
            } else if (role.equals(UserTypes.ADMIN.getSpringRole())) {
                event.forwardTo("admin");
            } else if (role.equals(UserTypes.URUN_SORUMLUSU.getSpringRole())) {
                event.forwardTo("po_main");
            } else if (role.equals(UserTypes.YAZILIM_YONETICISI.getSpringRole())) {
                event.forwardTo("supervisor_main");
            } else {
                add(new H1("Rol Bulunamadi: " + role));
            }
        } else {
            event.forwardTo("login");
        }
    }
}
