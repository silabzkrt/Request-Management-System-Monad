package org.vaadin.example.domain.customer.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.example.domain.company.dao.CompanyRepository;
import org.vaadin.example.domain.company.model.Company;
import org.vaadin.example.domain.notification.service.UserNotificationService;
import org.vaadin.example.domain.notification.view.NotificationBadge;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.request.service.RequestNoteService;
import org.vaadin.example.domain.request.view.components.RequestNoteComponent;
import org.vaadin.example.domain.request.service.RequestService;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.vaadin.example.shared.enums.TaskType;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.vaadin.flow.component.dependency.CssImport;
import org.vaadin.example.shared.enums.*;

import org.vaadin.example.domain.common.view.MainLayout;

@Route(value = "customer_main", layout = MainLayout.class)
@CssImport("./styles/styles.css")
@CssImport(value = "./styles/grid-styles.css", themeFor = "vaadin-grid")
@RolesAllowed("MUSTERI")
@PageTitle("Customer Dashboard | Monad")
public class CustomerMainPage extends HorizontalLayout {

    private final RequestService requestService;
    private final UserService userService;
    private final CompanyRepository companyRepository;
    private final UserNotificationService notificationService;
    private final RequestNoteService requestNoteService;
    private User currentUser;
    private VerticalLayout sidebar;

    private Grid<Request> requestGrid;

    public CustomerMainPage(RequestService requestService, UserService userService, CompanyRepository companyRepository, RequestNoteService requestNoteService, UserNotificationService notificationService) {
        this.requestService = requestService;
        this.userService = userService;
        this.companyRepository = companyRepository;
        this.requestNoteService = requestNoteService;
        this.notificationService = notificationService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        // Ana arkaplan gradienti (Login view ile uyumlu)
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

        com.vaadin.flow.component.html.Image logo = new com.vaadin.flow.component.html.Image("images/logo.png", "Monad Logo");
        logo.setHeight("80px");
        logo.getStyle()
                .set("margin", "20px auto 40px auto")
                .set("display", "block");

        // "Create Request" Button (Grid üstünde)
        Button createReqBtn = new Button("Talep Oluştur", e -> openCreateRequestDialog());
        createReqBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createReqBtn.getStyle().set("background-color", "#3b6998").set("padding", "20px 40px");
        
        HorizontalLayout toolbar = new HorizontalLayout(createReqBtn);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.CENTER);
        toolbar.getStyle().set("margin-bottom", "20px");

        requestGrid = new Grid<>(Request.class, false);
        requestGrid.getStyle().set("background", "rgba(255, 255, 255, 0.8)").set("border-radius", "8px");

        requestGrid.addColumn(Request::getTitle).setHeader("Başlık").setAutoWidth(true);
        requestGrid.addColumn(r -> {
            String desc = r.getDescription();
            if (desc == null) return "";
            return desc.length() > 100 ? desc.substring(0, 100) + "..." : desc;
        }).setHeader("Açıklama").setAutoWidth(true);
        
        requestGrid.addColumn(new com.vaadin.flow.data.renderer.ComponentRenderer<>(req -> {
            Span statusSpan = new Span(req.getStatus() != null ? req.getStatus().name() : "UNKNOWN");
            statusSpan.getStyle().set("padding", "4px 8px").set("border-radius", "4px").set("font-weight", "600");
            
            if (req.getStatus() != null) {
                switch (req.getStatus()) {
                    case PENDING: statusSpan.getStyle().set("background-color", "#fff3cd").set("color", "#856404"); break;
                    case REJECTED: statusSpan.getStyle().set("background-color", "#f8d7da").set("color", "#721c24"); break;
                    case UNASSIGNED: statusSpan.getStyle().set("background-color", "#d1ecf1").set("color", "#0c5460"); break;
                    case ASSIGNED: statusSpan.getStyle().set("background-color", "#cce5ff").set("color", "#004085"); break;
                    case TESTING: statusSpan.getStyle().set("background-color", "#e2e3e5").set("color", "#383d41"); break;
                    case COMPLETED: statusSpan.getStyle().set("background-color", "#d4edda").set("color", "#155724"); break;
                    case EDITED: statusSpan.getStyle().set("background-color", "#e2d9f3").set("color", "#3b1c60"); break;
                }
            }
            return statusSpan;
        })).setHeader("Durum").setAutoWidth(true);
        
        // Aksiyon Butonları
        requestGrid.addComponentColumn(req -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            Button detailsBtn = new Button("Detayları Görüntüle", e -> openDetailsDialog(req));
            detailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            detailsBtn.getStyle().set("background-color", "#3b6998");
            
            Button editBtn = new Button("Düzenle", e -> openEditDialog(req));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            editBtn.getStyle().set("background-color", "#3b6998");
            editBtn.setEnabled(req.isEditable()); 
            
            actions.add(detailsBtn, editBtn);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        updateGrid();

        main.add(logo, toolbar, requestGrid);
        return main;
    }

    private void updateGrid() {
        requestGrid.setItems(requestService.findByCreator(currentUser));
    }

    private void openCreateRequestDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Talep Oluştur");

        FormLayout formLayout = new FormLayout();
        
        TextField titleField = new TextField("Başlık");
        TextArea descField = new TextArea("Açıklama");
        descField.setMinHeight("100px");
        
        ComboBox<TaskType> typeField = new ComboBox<>("Talep Tipi");
        typeField.setItems(TaskType.values());
        
        DatePicker deadlineField = new DatePicker("Deadline (İsteğe Bağlı)");
        
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        upload.setMaxFiles(5);
        Span uploadHint = new Span("Maksimum 5 dosya yükleyebilirsiniz.");
        uploadHint.getStyle().set("font-size", "12px").set("color", "gray");
        
        final java.util.List<String> uploadedPathsList = new java.util.ArrayList<>();
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            File uploadsDir = new File(System.getProperty("user.dir") + "/uploads/");
            if (!uploadsDir.exists()) uploadsDir.mkdirs();
            try {
                File targetFile = new File(uploadsDir, System.currentTimeMillis() + "_" + fileName);
                Files.copy(buffer.getInputStream(fileName), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                uploadedPathsList.add(targetFile.getAbsolutePath());
            } catch (Exception ex) {
                Notification.show("Dosya yüklenemedi: " + ex.getMessage());
            }
        });

        formLayout.add(titleField, descField, typeField, deadlineField, upload, uploadHint);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        
        dialog.add(formLayout);

        Button saveBtn = new Button("Talep Oluştur", e -> {
            if (titleField.isEmpty() || descField.isEmpty()) {
                Notification.show("Başlık ve Açıklama Gerekli");
                return;
            }
            Request newReq = new Request(
                currentUser,
                titleField.getValue(),
                descField.getValue(),
                typeField.getValue(),
                deadlineField.getValue()
            );
            if (!uploadedPathsList.isEmpty()) {
                newReq.setAttachmentPath(String.join(";", uploadedPathsList));
            }
            requestService.save(newReq);
            Notification.show("Talep başarıyla alındı");
            updateGrid();
            dialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background-color", "#3b6998");
        
        Button cancelBtn = new Button("Çıkış", e -> dialog.close());

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openDetailsDialog(Request req) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setMaxHeight("90vh");
        dialog.setHeaderTitle("TALEP ID " + req.getId() + " DETAYLARI");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(false);
        layout.setPadding(false);

        layout.add(new Div(new Span("Talep Başlığı: "), new Span(req.getTitle() != null ? req.getTitle() : "Bilinmeyen")));
        
        Span descSpan = new Span(req.getDescription() != null ? req.getDescription() : "Yok");
        descSpan.getStyle().set("white-space", "pre-wrap").set("word-break", "break-word");
        layout.add(new Div(new Span("Açıklama: "), descSpan));
        
        layout.add(new Div(new Span("Talebi Oluşturan: "), new Span(req.getCreator() != null ? (req.getCreator().getNameSurname() + " " + req.getCreator().getId().toString()) : "Bilinmeyen")));
        layout.add(new Div(new Span("Müşteri ID: "), new Span(req.getCreator() != null && req.getCreator().getId() != null ? req.getCreator().getId().toString() : "Bilinmeyen")));
        layout.add(new Div(new Span("Tip: "), new Span(req.getType() != null ? req.getType().name() : "None")));
        layout.add(new Div(new Span("Teslim: "), new Span(req.getDeadline() != null ? req.getDeadline().toString() : "None")));
        layout.add(new Div(new Span("Durum: "), new Span(req.getStatus() != null ? req.getStatus().name() : "Bilinmeyen")));
        
        if (req.getAttachmentPath() != null && !req.getAttachmentPath().trim().isEmpty()) {
            Div attachmentsDiv = new Div();
            attachmentsDiv.add(new Span("Ekli Dosya(lar): "));
            String[] paths = req.getAttachmentPath().split(";");
            for (String p : paths) {
                java.io.File file = new java.io.File(p);
                if (file.exists()) {
                    com.vaadin.flow.server.StreamResource resource = new com.vaadin.flow.server.StreamResource(
                        file.getName(), () -> {
                            try { return new java.io.FileInputStream(file); }
                            catch (Exception ex) { return null; }
                        });
                    com.vaadin.flow.component.html.Anchor downloadLink = new com.vaadin.flow.component.html.Anchor(resource, file.getName());
                    downloadLink.setTarget("_blank");
                    downloadLink.getStyle().set("margin-right", "10px");
                    attachmentsDiv.add(downloadLink);
                }
            }
            layout.add(attachmentsDiv);
        } else {
            layout.add(new Div(new Span("Ekli Dosya: "), new Span("Yok")));
        }

        layout.add(new H3("Müşteri ile Yazışmalar"));
        RequestNoteComponent noteComponent = new RequestNoteComponent(requestNoteService, req, currentUser, false);
        layout.add(noteComponent);

        dialog.add(layout);
        
        Button closeBtn = new Button("İptal", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeBtn);
        
        dialog.open();
    }

    private void openEditDialog(Request req) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Düzenle " + req.getId());

        FormLayout formLayout = new FormLayout();
        
        TextField titleField = new TextField("Başlık");
        titleField.setValue(req.getTitle());
        
        TextArea descField = new TextArea("Açıklama");
        descField.setValue(req.getDescription());
        descField.setMinHeight("100px");
        
        ComboBox<TaskType> typeField = new ComboBox<>("Talep Tipi");
        typeField.setItems(TaskType.values());
        typeField.setValue(req.getType());
        
        DatePicker deadlineField = new DatePicker("Deadline (İsteğe Bağlı)");
        deadlineField.setValue(req.getDeadline());

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        upload.setMaxFiles(5);
        Span uploadHint = new Span("Yeni dosya eklerseniz üzerine eklenir. Maks 5 dosya.");
        uploadHint.getStyle().set("font-size", "12px").set("color", "gray");
        
        final java.util.List<String> uploadedPathsList = new java.util.ArrayList<>();
        if (req.getAttachmentPath() != null && !req.getAttachmentPath().trim().isEmpty()) {
            uploadedPathsList.addAll(java.util.Arrays.asList(req.getAttachmentPath().split(";")));
        }

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            File uploadsDir = new File(System.getProperty("user.dir") + "/uploads/");
            if (!uploadsDir.exists()) uploadsDir.mkdirs();
            try {
                File targetFile = new File(uploadsDir, System.currentTimeMillis() + "_" + fileName);
                Files.copy(buffer.getInputStream(fileName), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                uploadedPathsList.add(targetFile.getAbsolutePath());
            } catch (Exception ex) {
                Notification.show("Dosya yüklenemedi: " + ex.getMessage());
            }
        });

        formLayout.add(titleField, descField, typeField, deadlineField, upload, uploadHint);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        
        dialog.add(formLayout);

        Button saveBtn = new Button("Değişiklikleri Kaydet", e -> {
            if (titleField.isEmpty() || descField.isEmpty()) {
                Notification.show("Başlık ve Açıklama Gerekli!");
                return;
            }
            req.setTitle(titleField.getValue());
            req.setDescription(descField.getValue());
            req.setType(typeField.getValue());
            req.setDeadline(deadlineField.getValue());
            if (!uploadedPathsList.isEmpty()) {
                req.setAttachmentPath(String.join(";", uploadedPathsList));
            }
            
            
            boolean wasAlreadyAssigned = req.getStatus() == RequestStatus.ASSIGNED || req.getStatus() == RequestStatus.TESTING || req.getStatus() == RequestStatus.COMPLETED;
            if (!wasAlreadyAssigned) {
                req.setStatus(RequestStatus.EDITED);
                
                // Notify Evaluators
                if (req.getCreator() != null && req.getCreator().getCompany() != null) {
                    List<User> supervisors = userService.findByRoleAndCompany(UserTypes.YAZILIM_YONETICISI.getSpringRole(), req.getCreator().getCompany());
                    for (User s : supervisors) {
                        notificationService.sendNotification(s, "Talep müşteri tarafından güncellendi: " + req.getTitle(), req);
                    }
                    List<User> productOwners = userService.findByRoleAndCompany(UserTypes.URUN_SORUMLUSU.getSpringRole(), req.getCreator().getCompany());
                    for (User po : productOwners) {
                        notificationService.sendNotification(po, "Talep müşteri tarafından güncellendi: " + req.getTitle(), req);
                    }
                }
            }
            
            requestService.save(req);
            Notification.show("Talep başarıyla güncellendi");
            updateGrid();
            dialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background-color", "#3b6998");
        
        Button closeBtn = new Button("İptal", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(closeBtn, saveBtn);
        dialog.open();
    }

    private void openUpdateProfileDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Profili Düzenle");

        FormLayout formLayout = new FormLayout();
        TextField nameField = new TextField("Ad Soyad");
        nameField.setValue(currentUser.getNameSurname() != null ? currentUser.getNameSurname() : "");

        PasswordField passwordField = new PasswordField("Yeni Şifre");
        passwordField.setPlaceholder("Değiştirmek istemiyorsanız boş bırakın");

        formLayout.add(nameField, passwordField);
        dialog.add(formLayout);

        Button saveBtn = new Button("Kaydet", e -> {
            if (!nameField.isEmpty()) {
                currentUser.setNameSurname(nameField.getValue());
            }
            if (!passwordField.isEmpty()) {
                // In a real app, hash password here using PasswordEncoder.
                // Assuming raw for now as user just changes string, but better handled via service if injected.
                currentUser.setPassword(passwordField.getValue());
            }
            userService.save(currentUser);
            Notification.show("Profil başarıyla güncellendi. Yenilemek için sayfayı tazeleyin.");
            dialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background-color", "#3b6998");
        
        Button cancelBtn = new Button("İptal", e -> dialog.close());
        
        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

}
