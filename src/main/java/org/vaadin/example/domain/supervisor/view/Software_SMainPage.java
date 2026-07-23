package org.vaadin.example.domain.supervisor.view;

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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.example.domain.notification.service.UserNotificationService;
import org.vaadin.example.domain.notification.view.NotificationBadge;
import org.vaadin.example.domain.priority.model.Priority;
import org.vaadin.example.domain.priority.service.PriorityCalculationService;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.request.service.RequestNoteService;
import org.vaadin.example.domain.request.view.components.RequestNoteComponent;
import org.vaadin.example.domain.request.service.RequestService;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.vaadin.example.shared.enums.RequestStatus;
import org.vaadin.example.shared.enums.TaskType;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.dependency.CssImport;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.vaadin.example.domain.common.view.MainLayout;

@Route(value = "supervisor_main", layout = MainLayout.class)
@CssImport("./styles/styles.css")
@CssImport(value = "./styles/grid-styles.css", themeFor = "vaadin-grid")
@RolesAllowed("YAZILIM_YONETICISI")
@PageTitle("Supervisor Dashboard | Monad")
public class Software_SMainPage extends HorizontalLayout {

    private final RequestService requestService;
    private final RequestNoteService requestNoteService;
    private final UserService userService;
    private final PriorityCalculationService priorityCalculationService;
    private final UserNotificationService notificationService;
    private User currentUser;
    private VerticalLayout sidebar;
    private Grid<Request> requestGrid;

    public Software_SMainPage(RequestService requestService, 
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

        HorizontalLayout filterBar = new HorizontalLayout();
        filterBar.setWidthFull();
        filterBar.setAlignItems(Alignment.BASELINE);
        
        ComboBox<TaskType> typeFilter = new ComboBox<>("Talep Tipi");
        typeFilter.setItems(TaskType.values());
        typeFilter.setClearButtonVisible(true);
        
        TextField companyFilter = new TextField("Şirket Ara");
        companyFilter.setClearButtonVisible(true);

        com.vaadin.flow.component.checkbox.Checkbox showPastCheckbox = new com.vaadin.flow.component.checkbox.Checkbox("Geçmiş/Reddedilen Talepleri Göster");

        Button applyFilterBtn = new Button("Filtrele", e -> updateGrid(typeFilter.getValue(), companyFilter.getValue(), showPastCheckbox.getValue()));
        applyFilterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyFilterBtn.getStyle().set("background-color", "#3b6998");
        
        filterBar.add(typeFilter, companyFilter, showPastCheckbox, applyFilterBtn);

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

        updateGrid(null, null, false);

        main.add(header, logo, filterBar, requestGrid);
        return main;
    }

    private void updateGrid(TaskType type, String company, boolean showPast) {
        List<Request> displayedReqs = new java.util.ArrayList<>();
        if (showPast) {
            displayedReqs.addAll(requestService.findPastForSupervisor());
        } else {
            displayedReqs.addAll(requestService.findPendingForSupervisor());
        }
        
        displayedReqs.sort((r1, r2) -> {
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
            displayedReqs.removeIf(r -> r.getType() != type);
        }
        if (company != null && !company.trim().isEmpty()) {
            displayedReqs.removeIf(r -> r.getCreator() == null || r.getCreator().getCompany() == null || 
                                    !r.getCreator().getCompany().getCompanyName().toLowerCase().contains(company.toLowerCase()));
        }
        
        requestGrid.setItems(displayedReqs);
    }

    private void openEvaluationDialog(Request req) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Talebi Değerlendir (ID: " + req.getId() + ")");
        dialog.setWidth("1200px");
        dialog.setMaxHeight("90vh");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();

        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setWidth("33%");
        detailsLayout.getStyle().set("background", "#f8f9fa").set("padding", "15px").set("border-radius", "8px");

        detailsLayout.add(new H3("Talep Detayları"));
        detailsLayout.add(new Div(new Span("Başlık: "), new Span(req.getTitle())));
        detailsLayout.add(new Div(new Span("Açıklama: "), new Span(req.getDescription())));
        detailsLayout.add(new Div(new Span("Müşteri: "), new Span(req.getCreator() != null ? req.getCreator().getNameSurname() : "-")));
        detailsLayout.add(new Div(new Span("Şirket: "), new Span(req.getCreator() != null && req.getCreator().getCompany() != null ? req.getCreator().getCompany().getCompanyName() : "-")));
        detailsLayout.add(new Div(new Span("Tip: "), new Span(req.getType() != null ? req.getType().name() : "-")));
        detailsLayout.add(new Div(new Span("Deadline: "), new Span(req.getDeadline() != null ? req.getDeadline().toString() : "-")));

        Priority p = req.getPriority();
        if (p != null && p.getProductMgrScore() != null) {
            detailsLayout.add(new H3("Ürün Yöneticisi Değerlendirmesi"));
            detailsLayout.add(new Div(new Span("Müşteri Değeri: "), new Span(p.getCustomerRank() != null ? String.valueOf(p.getCustomerRank()) : "-")));
            detailsLayout.add(new Div(new Span("Ürün Yöneticisi Skoru: "), new Span(String.valueOf(p.getProductMgrScore()))));
            detailsLayout.add(new Div(new Span("Ödeme Önceliği: "), new Span(p.getPaymentPriority() != null ? String.valueOf(p.getPaymentPriority()) : "-")));
        } else {
            detailsLayout.add(new Div(new Span("Ürün Yöneticisi henüz değerlendirmedi.")));
        }

        if (req.getAttachmentPath() != null && !req.getAttachmentPath().trim().isEmpty()) {
            Div attachmentsDiv = new Div();
            attachmentsDiv.add(new Span("Ekli Dosya(lar): "));
            String[] paths = req.getAttachmentPath().split(";");
            for (String pathStr : paths) {
                java.io.File file = new java.io.File(pathStr);
                if (file.exists()) {
                    com.vaadin.flow.server.StreamResource resource = new com.vaadin.flow.server.StreamResource(
                        file.getName(), () -> {
                            try { return new java.io.FileInputStream(file); }
                            catch (Exception ex) { return null; }
                        });
                    Anchor downloadLink = new Anchor(resource, file.getName());
                    downloadLink.setTarget("_blank");
                    downloadLink.getStyle().set("margin-right", "10px");
                    attachmentsDiv.add(downloadLink);
                }
            }
            detailsLayout.add(attachmentsDiv);
        }

        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.setWidth("33%");
        centerLayout.setPadding(false);
        centerLayout.add(new H3("Müşteri ile Yazışmalar"));
        RequestNoteComponent publicNoteComponent = new RequestNoteComponent(requestNoteService, req, currentUser, false);
        centerLayout.add(publicNoteComponent);

        VerticalLayout rightLayout = new VerticalLayout();
        rightLayout.setWidth("33%");
        rightLayout.setPadding(false);

        rightLayout.add(new H3("İç Notlar (Sadece Şirket)"));
        RequestNoteComponent internalNoteComponent = new RequestNoteComponent(requestNoteService, req, currentUser, true);
        rightLayout.add(internalNoteComponent);

        VerticalLayout scoringArea = new VerticalLayout();
        scoringArea.setPadding(false);
        scoringArea.getStyle().set("margin-top", "20px");

        IntegerField softwareMgrScoreField = new IntegerField("Yazılım Yöneticisi Skoru");
        softwareMgrScoreField.setMin(1);
        softwareMgrScoreField.setMax(5);
        if (p != null && p.getSoftwareMgrScore() != null) {
            softwareMgrScoreField.setValue(p.getSoftwareMgrScore());
        }

        scoringArea.add(new H3("Puanlama"), softwareMgrScoreField);

        rightLayout.add(scoringArea);
        mainLayout.add(detailsLayout, centerLayout, rightLayout);

        dialog.add(mainLayout);

        Button saveBtn = new Button("Puanla ve Kaydet", e -> {
            if (softwareMgrScoreField.getValue() == null || softwareMgrScoreField.getValue() < 1 || softwareMgrScoreField.getValue() > 5) {
                Notification.show("Yazılım Yöneticisi Skoru 1 ile 5 arasında olmalıdır!");
                return;
            }

            Priority prio = req.getPriority();
            if (prio == null) {
                prio = new Priority(req, req.getCreator() != null && req.getCreator().getCompany() != null ? req.getCreator().getCompany().getCompanyPoints() : null);
                req.setPriority(prio);
            }
            prio.setSoftwareMgrScore(softwareMgrScoreField.getValue());
            
            boolean ready = priorityCalculationService.attemptCalculate(prio);
            requestService.save(req);
            
            if (ready) {
                Notification.show("Her iki yönetici de puanladı, nihai skor hesaplandı ve atamaya hazır!");
            } else {
                Notification.show("Değerlendirmeniz kaydedildi (PO onayı bekleniyor).");
            }
            updateGrid(null, null, false);
            dialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background-color", "#28a745");

        Button closeBtn = new Button("İptal", e -> dialog.close());

        dialog.getFooter().add(closeBtn, saveBtn);
        dialog.open();
    }


}
