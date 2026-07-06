package org.vaadin.example.domain.productowner.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.example.domain.common.view.MainLayout;
import org.vaadin.example.domain.notification.service.UserNotificationService;
import org.vaadin.example.domain.notification.view.NotificationBadge;
import org.vaadin.example.domain.priority.model.Priority;
import org.vaadin.example.domain.priority.service.PriorityCalculationService;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.request.view.components.RequestNoteComponent;
import org.vaadin.example.domain.request.service.RequestService;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.vaadin.example.shared.enums.RequestStatus;
import org.vaadin.example.shared.enums.TaskType;
import com.vaadin.flow.component.dependency.CssImport;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route(value = "po_main", layout = MainLayout.class)
@CssImport("./styles/styles.css")
@CssImport(value = "./styles/grid-styles.css", themeFor = "vaadin-grid")
@RolesAllowed("URUN_SORUMLUSU")
@PageTitle("Product Owner Dashboard | Monad")
public class ProductOwnerMainPage extends HorizontalLayout {

    private final RequestService requestService;
    private final RequestNoteService requestNoteService;
    private final UserService userService;
    private final PriorityCalculationService priorityCalculationService;
    private final UserNotificationService notificationService;
    private User currentUser;
    private VerticalLayout sidebar;

    private Grid<Request> requestGrid;

    public ProductOwnerMainPage(RequestService requestService, 
                                RequestNoteService requestNoteService,
                                UserService userService,
                                PriorityCalculationService priorityCalculationService,
                                UserNotificationService notificationService) {
        this.requestService = requestService;
        this.requestNoteService = requestNoteService;
        this.userService = userService;
        this.priorityCalculationService = priorityCalculationService;
        this.notificationService = notificationService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        getStyle().set("background", "linear-gradient(to bottom, #ffffff 40%, #7da2cc 100%)");
        getStyle().set("font-family", "'Inter', sans-serif");

        loadCurrentUser();

        if (currentUser == null) {
            add(new H2("Kullanıcı bulunamadı. Lütfen tekrar giriş yapın."));
            return;
        }

        VerticalLayout mainContent = createMainContent();
        add(mainContent);
    }

    private void loadCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.findByEmail(email);
        userOpt.ifPresent(user -> this.currentUser = user);
    }

    private VerticalLayout createMainContent() {
        VerticalLayout main = new VerticalLayout();
        main.setHeightFull();
        main.setPadding(true);
        main.getStyle().set("padding-left", "40px").set("padding-right", "40px");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        String nameSurname = currentUser.getNameSurname() != null ? currentUser.getNameSurname() : "Kullanıcı";
        H2 welcomeText = new H2("HOŞGELDİN \"" + nameSurname.toUpperCase() + "\"");
        welcomeText.getStyle().set("font-weight", "400").set("letter-spacing", "2px").set("margin", "0 auto").set("text-align", "center").set("width", "100%");

        HorizontalLayout headerRight = new HorizontalLayout();
        headerRight.setAlignItems(Alignment.CENTER);
        NotificationBadge notificationBadge = new NotificationBadge(notificationService, currentUser);
        headerRight.add(notificationBadge);

        header.add(welcomeText, headerRight);
        header.expand(welcomeText);

        Image logo = new Image("images/logo.png", "Monad Logo");
        logo.setHeight("80px");
        logo.getStyle()
                .set("margin", "20px auto 40px auto")
                .set("display", "block");

        // Filter Bar
        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.setAlignItems(Alignment.BASELINE);
        
        ComboBox<TaskType> typeFilter = new ComboBox<>("Talep Tipi");
        typeFilter.setItems(TaskType.values());
        typeFilter.setClearButtonVisible(true);
        
        TextField companyFilter = new TextField("Şirket Ara");
        companyFilter.setClearButtonVisible(true);

        Button applyFilterBtn = new Button("Filtrele", e -> updateGrid(typeFilter.getValue(), companyFilter.getValue()));
        applyFilterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyFilterBtn.getStyle().set("background-color", "#3b6998");
        
        filterBar.add(typeFilter, companyFilter, applyFilterBtn);

        requestGrid = new Grid<>(Request.class, false);
        requestGrid.getStyle().set("background", "rgba(255, 255, 255, 0.8)").set("border-radius", "8px");

        requestGrid.addColumn(req -> req.getCreator() != null && req.getCreator().getCompany() != null ? req.getCreator().getCompany().getCompanyName() : "-").setHeader("Şirket").setAutoWidth(true);
        requestGrid.addColumn(Request::getTitle).setHeader("Başlık").setAutoWidth(true);
        requestGrid.addColumn(req -> req.getType() != null ? req.getType().name() : "-").setHeader("Tip").setAutoWidth(true);
        
        requestGrid.addColumn(new com.vaadin.flow.data.renderer.ComponentRenderer<>(req -> {
            Span statusSpan = new Span(req.getStatus() != null ? req.getStatus().name() : "UNKNOWN");
            statusSpan.getStyle().set("padding", "4px 8px").set("border-radius", "4px").set("font-weight", "600");
            statusSpan.getStyle().set("background-color", "#fff3cd").set("color", "#856404");
            return statusSpan;
        })).setHeader("Durum").setAutoWidth(true);
        
        requestGrid.addComponentColumn(req -> {
            Button evaluateBtn = new Button("Değerlendir", e -> openEvaluationDialog(req));
            evaluateBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            evaluateBtn.getStyle().set("background-color", "#28a745");
            return evaluateBtn;
        }).setHeader("Actions").setAutoWidth(true);

        updateGrid(null, null);

        main.add(header, logo, filterBar, requestGrid);
        return main;
    }

    private void updateGrid(TaskType type, String company) {
        List<Request> pendingReqs = requestService.findPendingForPO();
        
        pendingReqs.sort((r1, r2) -> {
            double w1 = org.vaadin.example.domain.priority.service.PriorityCalculationService.getTaskTypeMultiplier(r1.getType());
            double w2 = org.vaadin.example.domain.priority.service.PriorityCalculationService.getTaskTypeMultiplier(r2.getType());
            if (Double.compare(w2, w1) != 0) {
                return Double.compare(w2, w1);
            }
            if (r1.getCreatedAt() != null && r2.getCreatedAt() != null) {
                return r2.getCreatedAt().compareTo(r1.getCreatedAt());
            }
            return 0;
        });

        if (type != null) {
            pendingReqs.removeIf(r -> r.getType() != type);
        }
        if (company != null && !company.trim().isEmpty()) {
            pendingReqs.removeIf(r -> r.getCreator() == null || r.getCreator().getCompany() == null || 
                                    !r.getCreator().getCompany().getCompanyName().toLowerCase().contains(company.toLowerCase()));
        }
        
        requestGrid.setItems(pendingReqs);
    }

    private void openEvaluationDialog(Request req) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Talebi Değerlendir (ID: " + req.getId() + ")");
        dialog.setWidth("800px");
        dialog.setMaxHeight("90vh");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();

        // Sol taraf: Talep Detayları (Salt-Okunur)
        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setWidth("50%");
        detailsLayout.getStyle().set("background", "#f8f9fa").set("padding", "15px").set("border-radius", "8px");

        detailsLayout.add(new H3("Talep Detayları"));
        detailsLayout.add(new Div(new Span("Başlık: "), new Span(req.getTitle())));
        detailsLayout.add(new Div(new Span("Açıklama: "), new Span(req.getDescription())));
        detailsLayout.add(new Div(new Span("Müşteri: "), new Span(req.getCreator() != null ? req.getCreator().getNameSurname() : "-")));
        detailsLayout.add(new Div(new Span("Şirket: "), new Span(req.getCreator() != null && req.getCreator().getCompany() != null ? req.getCreator().getCompany().getCompanyName() : "-")));
        detailsLayout.add(new Div(new Span("Tip: "), new Span(req.getType() != null ? req.getType().name() : "-")));
        detailsLayout.add(new Div(new Span("Deadline: "), new Span(req.getDeadline() != null ? req.getDeadline().toString() : "-")));

        if (req.getAttachmentPath() != null) {
            java.io.File file = new java.io.File(req.getAttachmentPath());
            if (file.exists()) {
                com.vaadin.flow.server.StreamResource resource = new com.vaadin.flow.server.StreamResource(
                    file.getName(), () -> {
                        try { return new java.io.FileInputStream(file); }
                        catch (Exception ex) { return null; }
                    });
                Anchor downloadLink = new Anchor(resource, "Dosyayı İndir");
                downloadLink.setTarget("_blank");
                detailsLayout.add(new Div(new Span("Ek: "), downloadLink));
            }
        }

        // Sağ taraf: Notlar ve Puanlama
        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setWidth("50%");
        rightLayout.setPadding(false);

        // Notlar Kısmı (Bileşen ile)
        RequestNoteComponent noteComponent = new RequestNoteComponent(requestNoteService, req, currentUser);
        
        // Puanlama Kısmı
        VerticalLayout scoringArea = new VerticalLayout();
        scoringArea.setPadding(false);
        scoringArea.getStyle().set("margin-top", "20px");

        Priority priority = req.getPriority();
        if (priority == null) {
            priority = new Priority(req, req.getCreator() != null && req.getCreator().getCompany() != null ? req.getCreator().getCompany().getCompanyPoints() : null);
            req.setPriority(priority);
        }

        IntegerField customerRankField = new IntegerField("Customer Rank (örn: 1-10)");
        customerRankField.setValue(priority.getCustomerRank());

        IntegerField productMgrScoreField = new IntegerField("Product Manager Score (1-5)");
        productMgrScoreField.setMin(1);
        productMgrScoreField.setMax(5);
        productMgrScoreField.setValue(priority.getProductMgrScore());

        NumberField paymentPriorityField = new NumberField("Payment Priority");
        if (priority.getPaymentPriority() != null) paymentPriorityField.setValue(priority.getPaymentPriority());

        com.vaadin.flow.component.checkbox.Checkbox highestPriorityCheckbox = new com.vaadin.flow.component.checkbox.Checkbox("Highest Priority (Acil Durum)");
        highestPriorityCheckbox.setValue(Boolean.TRUE.equals(priority.getHighestPriority()));
        highestPriorityCheckbox.getStyle().set("color", "#d9534f").set("font-weight", "bold");

        scoringArea.add(new H3("Puanlama"), customerRankField, productMgrScoreField, paymentPriorityField, highestPriorityCheckbox);

        rightLayout.add(new H3("Notlar (Herkes Görebilir)"), noteComponent, scoringArea);
        mainLayout.add(detailsLayout, rightLayout);

        dialog.add(mainLayout);

        Button saveBtn = new Button("Puanla ve Kaydet", e -> {
            if (productMgrScoreField.getValue() == null || productMgrScoreField.getValue() < 1 || productMgrScoreField.getValue() > 5) {
                Notification.show("Product Manager Score 1 ile 5 arasında olmalıdır!");
                return;
            }

            Priority p = req.getPriority();
            if (customerRankField.getValue() != null) p.setCustomerRank(customerRankField.getValue());
            if (productMgrScoreField.getValue() != null) p.setProductMgrScore(productMgrScoreField.getValue().intValue());
            if (paymentPriorityField.getValue() != null) p.setPaymentPriority(paymentPriorityField.getValue());
            p.setHighestPriority(highestPriorityCheckbox.getValue());
            
            // Eğer diğer puan da varsa nihai hesapla, yoksa sadece kaydet
            priorityCalculationService.attemptCalculate(p);
            
            requestService.save(req);
            Notification.show("Talebi değerlendirdiniz.");
            updateGrid(null, null);
            dialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background-color", "#28a745");

        Button rejectBtn = new Button("Talebi Reddet", e -> {
            requestService.rejectByPO(req);
            Notification.show("Talep Reddedildi.");
            updateGrid(null, null);
            dialog.close();
        });
        rejectBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button closeBtn = new Button("İptal", e -> dialog.close());

        dialog.getFooter().add(closeBtn, rejectBtn, saveBtn);
        dialog.open();
    }


}
