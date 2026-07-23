package org.vaadin.example.auth.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.example.domain.user.service.PasswordResetService;

@Route("forgot-password")
@PageTitle("Şifremi Unuttum")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout {

    private final PasswordResetService passwordResetService;

    public ForgotPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#f5f5f5");

        VerticalLayout card = new VerticalLayout();
        card.setWidth("400px");
        card.getStyle().set("background", "white")
                .set("padding", "30px")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)");
        card.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Şifremi Unuttum");
        Span description = new Span("Sisteme kayıtlı e-posta adresinizi giriniz. Şifre sıfırlama bağlantısı e-posta adresinize gönderilecektir.");
        description.getStyle().set("text-align", "center").set("color", "#666").set("margin-bottom", "20px");

        EmailField emailField = new EmailField("E-Posta Adresi");
        emailField.setWidthFull();
        emailField.setRequiredIndicatorVisible(true);

        Button submitBtn = new Button("Sıfırlama Bağlantısı Gönder", e -> {
            if (emailField.isEmpty() || emailField.isInvalid()) {
                Notification.show("Geçerli bir e-posta adresi giriniz.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            boolean success = passwordResetService.initiatePasswordReset(emailField.getValue());
            if (success) {
                Notification.show("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi.", 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                emailField.clear();
                UI.getCurrent().navigate("login");
            } else {
                // Güvenlik gereği e-posta sistemde kayıtlı olmasa bile "gönderildi" mesajı verebiliriz veya açıkça hata gösterebiliriz.
                Notification.show("Bu e-posta adresi ile kayıtlı bir kullanıcı bulunamadı.", 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.setWidthFull();
        
        Button backBtn = new Button("Giriş Sayfasına Dön", e -> UI.getCurrent().navigate("login"));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.setWidthFull();

        card.add(title, description, emailField, submitBtn, backBtn);
        add(card);
    }
}
