package org.vaadin.example.auth.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.example.domain.user.service.PasswordResetService;

import java.util.List;
import java.util.Map;

@Route("reset-password")
@PageTitle("Yeni Şifre Belirle")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements HasUrlParameter<String> {

    private final PasswordResetService passwordResetService;
    private String token;
    
    private final VerticalLayout card;
    private final PasswordField passwordField;
    private final PasswordField confirmPasswordField;
    private final Button submitBtn;
    private final Span errorSpan;

    public ResetPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#f5f5f5");

        card = new VerticalLayout();
        card.setWidth("400px");
        card.getStyle().set("background", "white")
                .set("padding", "30px")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)");
        card.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Yeni Şifre Belirle");
        
        errorSpan = new Span();
        errorSpan.getStyle().set("color", "red").set("text-align", "center");
        errorSpan.setVisible(false);

        passwordField = new PasswordField("Yeni Şifre");
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);
        
        confirmPasswordField = new PasswordField("Yeni Şifre (Tekrar)");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequiredIndicatorVisible(true);

        submitBtn = new Button("Şifreyi Güncelle", e -> updatePassword());
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.setWidthFull();

        card.add(title, errorSpan, passwordField, confirmPasswordField, submitBtn);
        add(card);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        Map<String, List<String>> parametersMap = event.getLocation().getQueryParameters().getParameters();
        if (parametersMap.containsKey("token")) {
            this.token = parametersMap.get("token").get(0);
        }

        if (this.token == null || this.token.isEmpty()) {
            showError("Geçersiz veya eksik sıfırlama bağlantısı.");
            return;
        }

        if (!passwordResetService.validatePasswordResetToken(this.token)) {
            showError("Bu sıfırlama bağlantısının süresi dolmuş veya geçersiz.");
        }
    }
    
    private void showError(String message) {
        errorSpan.setText(message);
        errorSpan.setVisible(true);
        passwordField.setEnabled(false);
        confirmPasswordField.setEnabled(false);
        submitBtn.setEnabled(false);
    }

    private void updatePassword() {
        if (passwordField.isEmpty() || confirmPasswordField.isEmpty()) {
            Notification.show("Lütfen tüm alanları doldurun.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            Notification.show("Şifreler eşleşmiyor.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        
        boolean success = passwordResetService.resetPassword(this.token, passwordField.getValue());
        if (success) {
            Notification.show("Şifreniz başarıyla güncellendi! Giriş yapabilirsiniz.", 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate("login");
        } else {
            Notification.show("Şifre güncellenirken bir hata oluştu. Lütfen bağlantınızı kontrol edin.", 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
