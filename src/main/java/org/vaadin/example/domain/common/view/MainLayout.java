package org.vaadin.example.domain.common.view;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLayout;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.example.domain.admin.view.AdminPage;
import org.vaadin.example.domain.customer.view.CustomerMainPage;
import org.vaadin.example.domain.developer.view.DeveloperMainPage;
import org.vaadin.example.domain.developer.view.DeveloperPastRequests;
import org.vaadin.example.domain.productowner.view.ProductOwnerMainPage;
import org.vaadin.example.domain.productowner.view.ProductOwnerpastRequests;
import org.vaadin.example.domain.supervisor.view.AssignTaskPage;
import org.vaadin.example.domain.supervisor.view.Software_SMainPage;
import org.vaadin.example.domain.supervisor.view.Software_SPastRequests;
import org.vaadin.example.domain.supervisor.view.SupervisorDeveloperScoresPage;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayout.Section;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H3;

public class MainLayout extends AppLayout {
    private final UserService userService;
    private User currentUser;

    @Autowired
    public MainLayout(UserService userService) {
        this.userService = userService;
        
        VerticalLayout sidebar = createSidebar();
        addToDrawer(sidebar);
        
        setPrimarySection(Section.DRAWER); getElement().getStyle().set("--vaadin-app-layout-drawer-width", "300px");
    }

    private VerticalLayout createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setWidth("100%"); sidebar.getStyle().set("overflow-x", "hidden").set("overflow-y", "auto");
        sidebar.setHeightFull();
        sidebar.getStyle()
                .set("background", "rgba(0, 0, 0, 0.75)")
                .set("backdrop-filter", "blur(10px)")
                .set("color", "white")
                .set("box-shadow", "2px 0 10px rgba(0,0,0,0.2)")
                .set("z-index", "1000"); // keep it above content
        sidebar.setPadding(true);
        sidebar.setSpacing(true);
        
        // Find user
        String email = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = "";
        if (authentication != null) {
            email = authentication.getName();
            if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
                role = authentication.getAuthorities().iterator().next().getAuthority();
            }
        }
        
        if (!email.isEmpty() && !"anonymousUser".equals(email)) {
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
            }
        }

        // Home Button
        Button homeBtn = new Button("Ana Ekrana Dön", VaadinIcon.HOME.create());
        homeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeBtn.getStyle().set("background-color", "#3b6998").set("width", "100%");
        final String fRole = role;
        homeBtn.addClickListener(e -> {
            if (fRole.equals("ROLE_ADMIN")) {
                UI.getCurrent().navigate(AdminPage.class);
            } else if (fRole.equals("ROLE_MUSTERI")) {
                UI.getCurrent().navigate(CustomerMainPage.class);
            } else if (fRole.equals("ROLE_YAZILIMCI")) {
                UI.getCurrent().navigate(DeveloperMainPage.class);
            } else if (fRole.equals("ROLE_URUN_SORUMLUSU")) {
                UI.getCurrent().navigate(ProductOwnerMainPage.class);
            } else if (fRole.equals("ROLE_YAZILIM_YONETICISI")) {
                UI.getCurrent().navigate(Software_SMainPage.class);
            }
        });
        sidebar.add(homeBtn);

        if (currentUser != null) {
            Div avatarPlaceholder = new Div();
            avatarPlaceholder.setWidth("120px");
            avatarPlaceholder.setHeight("120px");
            avatarPlaceholder.getStyle()
                    .set("background-color", "#3b6998")
                    .set("border-radius", "50%")
                    .set("margin", "20px auto")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("font-size", "40px")
                    .set("font-weight", "bold");
            
            String nameSurname = currentUser.getNameSurname() != null ? currentUser.getNameSurname() : "Kullanıcı";
            String initials = "U";
            if (!nameSurname.trim().isEmpty()) {
                String[] parts = nameSurname.trim().split("\\s+");
                initials = parts.length > 1 ? parts[0].substring(0,1) + parts[1].substring(0,1) : parts[0].substring(0,1);
            }
            avatarPlaceholder.setText(initials.toUpperCase());

            VerticalLayout detailsLayout = new VerticalLayout();
            detailsLayout.setPadding(false);
            detailsLayout.setSpacing(false);
            detailsLayout.getStyle().set("font-size", "14px").set("line-height", "1.6");

            detailsLayout.add(
                new Div(new Span("Kullanıcı: "), new Span(nameSurname)),
                new Div(new Span("Email: "), new Span(currentUser.getEmail())),
                new Div(new Span("Şirket: "), new Span(currentUser.getCompany() != null ? currentUser.getCompany().getCompanyName() : "None"))
            );
            
            sidebar.add(avatarPlaceholder, detailsLayout);
            sidebar.setHorizontalComponentAlignment(Alignment.CENTER, avatarPlaceholder);

            // Role specific buttons
            if (fRole.equals("ROLE_YAZILIM_YONETICISI")) {
                Button pastReqBtn = new Button("Geçmiş Talepler", e -> UI.getCurrent().navigate(Software_SPastRequests.class));
                pastReqBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                pastReqBtn.getStyle().set("background-color", "#28a745").set("width", "100%").set("margin-top", "20px");

                Button assignTaskBtn = new Button("Yazılımcıya Ata", e -> UI.getCurrent().navigate(AssignTaskPage.class));
                assignTaskBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                assignTaskBtn.getStyle().set("background-color", "#007bff").set("width", "100%").set("margin-top", "10px");
                
                Button devScoresBtn = new Button("Yazılımcı Performansları", e -> UI.getCurrent().navigate(SupervisorDeveloperScoresPage.class));
                devScoresBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                devScoresBtn.getStyle().set("background-color", "#17a2b8").set("width", "100%").set("margin-top", "10px");
                
                sidebar.add(pastReqBtn, assignTaskBtn, devScoresBtn);
            } else if (fRole.equals("ROLE_YAZILIMCI")) {
                Button pastReqBtn = new Button("Geçmiş İşler", e -> UI.getCurrent().navigate(DeveloperPastRequests.class));
                pastReqBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                pastReqBtn.getStyle().set("background-color", "#28a745").set("width", "100%").set("margin-top", "20px");
                sidebar.add(pastReqBtn);
            } else if (fRole.equals("ROLE_URUN_SORUMLUSU")) {
                Button pastReqBtn = new Button("Geçmiş Talepler", e -> UI.getCurrent().navigate(ProductOwnerpastRequests.class));
                pastReqBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                pastReqBtn.getStyle().set("background-color", "#28a745").set("width", "100%").set("margin-top", "20px");
                sidebar.add(pastReqBtn);
            } else if (fRole.equals("ROLE_MUSTERI")) {
                Button contactAdminBtn = new Button("Admine Bağlan", e -> com.vaadin.flow.component.UI.getCurrent().navigate("contact_admin"));
                contactAdminBtn.getStyle().set("width", "100%").set("margin-top", "10px").set("color", "white").set("background", "rgba(255,255,255,0.2)");
                sidebar.add(contactAdminBtn);
            }

            Button updateProfileBtn = new Button("Profili Düzenle", e -> openUpdateProfileDialog());
            updateProfileBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            updateProfileBtn.getStyle().set("background-color", "#3b6998").set("width", "100%").set("margin-top", "10px");
            sidebar.add(updateProfileBtn);
        }
        
        // Push logout to bottom
        Div spacer = new Div();
        spacer.getStyle().set("flex-grow", "1");
        sidebar.add(spacer);

        Button logoutBtn = new Button("Çıkış Yap", VaadinIcon.SIGN_OUT.create());
        logoutBtn.getStyle().set("color", "red").set("width", "100%").set("background-color", "transparent").set("box-shadow", "none");
        logoutBtn.addClickListener(e -> {
            com.vaadin.flow.server.VaadinSession.getCurrent().getSession().invalidate();
            com.vaadin.flow.server.VaadinSession.getCurrent().close();
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            com.vaadin.flow.component.UI.getCurrent().getPage().setLocation("/login");
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private void openUpdateProfileDialog() {
        if (currentUser == null) return;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Profili Düzenle");

        VerticalLayout layout = new VerticalLayout();
        TextField nameField = new TextField("Ad Soyad");
        nameField.setValue(currentUser.getNameSurname() != null ? currentUser.getNameSurname() : "");

        PasswordField passwordField = new PasswordField("Yeni Şifre");
        passwordField.setPlaceholder("Değiştirmek istemiyorsanız boş bırakın");

        layout.add(nameField, passwordField);
        dialog.add(layout);

        Button saveBtn = new Button("Kaydet", e -> {
            if (!nameField.isEmpty()) {
                currentUser.setNameSurname(nameField.getValue());
            }
            if (!passwordField.isEmpty()) {
                currentUser.setPassword(passwordField.getValue());
            }
            userService.save(currentUser);
            Notification.show("Profil güncellendi.");
            dialog.close();
            UI.getCurrent().getPage().reload();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelBtn = new Button("İptal", e -> dialog.close());
        
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }


}
