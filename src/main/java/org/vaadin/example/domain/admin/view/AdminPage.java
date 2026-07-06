package org.vaadin.example.domain.admin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.example.domain.common.view.MainLayout;
import org.vaadin.example.domain.company.model.Company;
import org.vaadin.example.domain.company.service.CompanyService;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;

@Route(value = "admin", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@PageTitle("Admin Paneli | Monad")
public class AdminPage extends VerticalLayout {

    private final UserService userService;
    private final CompanyService companyService;

    private Grid<User> userGrid;
    private Grid<Company> companyGrid;

    public AdminPage(UserService userService, CompanyService companyService) {
        this.userService = userService;
        this.companyService = companyService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Admin Paneli");
        add(title);

        Tab userTab = new Tab("Kullanıcı Yönetimi");
        Tab companyTab = new Tab("Şirket Yönetimi");
        Tabs tabs = new Tabs(userTab, companyTab);

        VerticalLayout userContent = createUserManagementContent();
        VerticalLayout companyContent = createCompanyManagementContent();
        companyContent.setVisible(false);

        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == userTab) {
                userContent.setVisible(true);
                companyContent.setVisible(false);
                refreshUserGrid();
            } else {
                userContent.setVisible(false);
                companyContent.setVisible(true);
                refreshCompanyGrid();
            }
        });

        add(tabs, userContent, companyContent);
    }

    // ==========================================
    // USER MANAGEMENT
    // ==========================================

    private VerticalLayout createUserManagementContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        userGrid = new Grid<>(User.class, false);
        userGrid.addColumn(User::getId).setHeader("ID").setAutoWidth(true);
        userGrid.addColumn(User::getNameSurname).setHeader("Ad Soyad").setAutoWidth(true);
        userGrid.addColumn(User::getEmail).setHeader("E-posta").setAutoWidth(true);
        userGrid.addColumn(User::getRole).setHeader("Rol").setAutoWidth(true);
        userGrid.addColumn(u -> u.getCompany() != null ? u.getCompany().getCompanyName() : "-").setHeader("Şirket").setAutoWidth(true);
        
        userGrid.addComponentColumn(user -> {
            Span status = new Span(user.isApproved() ? "Onaylı" : "Bekliyor");
            status.getElement().getThemeList().add(user.isApproved() ? "badge success" : "badge error");
            return status;
        }).setHeader("Durum").setAutoWidth(true);

        userGrid.addComponentColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            if (!user.isApproved()) {
                Button approveBtn = new Button("Onayla", VaadinIcon.CHECK.create());
                approveBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
                approveBtn.addClickListener(e -> approveUser(user));
                actions.add(approveBtn);
            }

            Button deleteBtn = new Button("Sil", VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteBtn.addClickListener(e -> confirmDeleteUser(user));
            actions.add(deleteBtn);

            return actions;
        }).setHeader("İşlemler").setAutoWidth(true);

        refreshUserGrid();
        layout.add(userGrid);
        return layout;
    }

    private void refreshUserGrid() {
        userGrid.setItems(userService.findAll());
    }

    private void approveUser(User user) {
        user.setApproved(true);
        userService.save(user);
        Notification.show(user.getNameSurname() + " başarıyla onaylandı.", 3000, Notification.Position.TOP_CENTER);
        refreshUserGrid();
    }

    private void confirmDeleteUser(User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Kullanıcıyı Sil");
        dialog.setText(user.getNameSurname() + " isimli kullanıcıyı silmek istediğinize emin misiniz? Bu işlem geri alınamaz!");
        dialog.setCancelable(true);
        dialog.setCancelText("İptal");
        dialog.setConfirmText("Sil");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            userService.delete(user.getId());
            Notification.show("Kullanıcı silindi.");
            refreshUserGrid();
        });
        dialog.open();
    }

    // ==========================================
    // COMPANY MANAGEMENT
    // ==========================================

    private VerticalLayout createCompanyManagementContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Button addCompanyBtn = new Button("Yeni Şirket Ekle", VaadinIcon.PLUS.create());
        addCompanyBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addCompanyBtn.addClickListener(e -> openAddCompanyDialog());

        companyGrid = new Grid<>(Company.class, false);
        companyGrid.addColumn(Company::getId).setHeader("ID").setAutoWidth(true);
        companyGrid.addColumn(Company::getCompanyName).setHeader("Şirket Adı").setAutoWidth(true);
        companyGrid.addColumn(Company::getCompanyPoints).setHeader("Puanı").setAutoWidth(true);

        companyGrid.addComponentColumn(company -> {
            Button deleteBtn = new Button("Sil", VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteBtn.addClickListener(e -> confirmDeleteCompany(company));
            return deleteBtn;
        }).setHeader("İşlemler").setAutoWidth(true);

        refreshCompanyGrid();
        layout.add(addCompanyBtn, companyGrid);
        return layout;
    }

    private void refreshCompanyGrid() {
        companyGrid.setItems(companyService.findAll());
    }

    private void openAddCompanyDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Yeni Şirket Ekle");

        TextField nameField = new TextField("Şirket Adı");
        NumberField pointsField = new NumberField("Puan (Örn: 50.0)");
        pointsField.setValue(50.0);

        VerticalLayout dialogLayout = new VerticalLayout(nameField, pointsField);
        dialog.add(dialogLayout);

        Button saveButton = new Button("Kaydet", e -> {
            if (nameField.isEmpty() || pointsField.isEmpty()) {
                Notification.show("Lütfen tüm alanları doldurun.", 3000, Notification.Position.MIDDLE);
                return;
            }
            Company company = new Company(nameField.getValue(), pointsField.getValue());
            companyService.save(company);
            Notification.show("Şirket başarıyla eklendi.");
            refreshCompanyGrid();
            dialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("İptal", e -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void confirmDeleteCompany(Company company) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Şirketi Sil");
        dialog.setText(company.getCompanyName() + " isimli şirketi silmek istediğinize emin misiniz? DİKKAT: Şirket silinirse bu şirkete bağlı tüm kullanıcılar ve talepler de (Cascade) silinebilir!");
        dialog.setCancelable(true);
        dialog.setCancelText("İptal");
        dialog.setConfirmText("Sil");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            try {
                companyService.delete(company.getId());
                Notification.show("Şirket silindi.");
                refreshCompanyGrid();
            } catch (Exception e) {
                Notification.show("Hata: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        dialog.open();
    }
}
