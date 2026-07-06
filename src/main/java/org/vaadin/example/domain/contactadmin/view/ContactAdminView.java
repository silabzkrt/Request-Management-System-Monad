package org.vaadin.example.domain.contactadmin.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.example.domain.contactadmin.model.ContactAdminMessage;
import org.vaadin.example.domain.contactadmin.service.ContactAdminMessageService;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Route("contact_admin")
@RolesAllowed({"MUSTERI", "URUN_SORUMLUSU", "YAZILIMCI", "YAZILIM_YONETICISI"})
@PageTitle("Contact Admin | Monad")
public class ContactAdminView extends VerticalLayout {

    private final ContactAdminMessageService messageService;
    private final UserService userService;
    private User currentUser;
    private String uploadedFilePath = null;

    public ContactAdminView(ContactAdminMessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        
        getStyle().set("background", "linear-gradient(to bottom, #ffffff 40%, #7da2cc 100%)");
        getStyle().set("font-family", "'Inter', sans-serif");

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            this.currentUser = userOpt.get();
        } else {
            add(new H2("Lütfen giriş yapın."));
            return;
        }

        VerticalLayout formCard = new VerticalLayout();
        formCard.setMaxWidth("600px");
        formCard.getStyle()
                .set("background", "rgba(255, 255, 255, 0.9)")
                .set("padding", "40px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 15px rgba(0,0,0,0.1)");
        
        H2 header = new H2("Contact Admin");
        header.getStyle().set("margin-top", "0").set("text-align", "center");

        TextField titleField = new TextField("Başlık (Title)");
        titleField.setWidthFull();

        TextArea descField = new TextArea("Açıklama (Description)");
        descField.setWidthFull();
        descField.setMinHeight("150px");

        // Upload Component
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        upload.setMaxFiles(1);
        
        Span uploadHint = new Span("Sadece JPEG, PNG, PDF ve DOCX");
        uploadHint.getStyle().set("font-size", "12px").set("color", "gray");
        
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            InputStream inputStream = buffer.getInputStream(fileName);
            
            File uploadsDir = new File("uploads/");
            if (!uploadsDir.exists()) {
                uploadsDir.mkdir();
            }
            
            try {
                File targetFile = new File(uploadsDir, System.currentTimeMillis() + "_" + fileName);
                Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                this.uploadedFilePath = targetFile.getAbsolutePath();
                Notification.show("Dosya yüklendi: " + fileName);
            } catch (Exception ex) {
                Notification.show("Dosya yüklenirken hata oluştu!");
                ex.printStackTrace();
            }
        });

        Button sendBtn = new Button("Gönder (Send)", e -> {
            if (titleField.isEmpty() || descField.isEmpty()) {
                Notification.show("Başlık ve açıklama zorunludur!");
                return;
            }
            
            ContactAdminMessage message = new ContactAdminMessage(
                currentUser,
                titleField.getValue(),
                descField.getValue(),
                uploadedFilePath
            );
            
            this.messageService.save(message);
            Notification.show("Mesajınız yöneticiye iletildi!");
            UI.getCurrent().navigate("customer_main");
        });
        sendBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendBtn.getStyle().set("background-color", "#3b6998").set("width", "100%");

        Button backBtn = new Button("Go Back Home", e -> UI.getCurrent().navigate("customer_main"));
        backBtn.getStyle().set("width", "100%").set("margin-top", "10px");

        formCard.add(header, titleField, descField, upload, uploadHint, sendBtn, backBtn);
        
        add(formCard);
    }
}
