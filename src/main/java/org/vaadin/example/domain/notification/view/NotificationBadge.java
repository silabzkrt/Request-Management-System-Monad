package org.vaadin.example.domain.notification.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.example.domain.notification.model.UserNotification;
import org.vaadin.example.domain.notification.service.UserNotificationService;
import org.vaadin.example.domain.user.model.User;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationBadge extends Div {

    private final UserNotificationService notificationService;
    private final User currentUser;
    private final Button bellButton;
    private final Span badge;

    private long lastUnreadCount = -1;

    public NotificationBadge(UserNotificationService notificationService, User currentUser) {
        this.notificationService = notificationService;
        this.currentUser = currentUser;

        this.getStyle().set("position", "relative").set("display", "inline-block");

        bellButton = new Button(VaadinIcon.BELL.create(), e -> openNotificationsDialog());
        bellButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        bellButton.getStyle().set("color", "#3b6998");

        badge = new Span();
        badge.getElement().getThemeList().add("badge error primary small");
        badge.getStyle()
                .set("position", "absolute")
                .set("top", "-5px")
                .set("right", "-5px")
                .set("padding", "2px 5px")
                .set("border-radius", "50%");

        add(bellButton, badge);
        updateBadge();
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        com.vaadin.flow.component.UI ui = attachEvent.getUI();
        ui.setPollInterval(3000); // Poll every 3 seconds

        lastUnreadCount = notificationService.getUnreadCount(currentUser);

        ui.addPollListener(e -> {
            long currentCount = notificationService.getUnreadCount(currentUser);
            if (lastUnreadCount != -1 && currentCount > lastUnreadCount) {
                // Play a beep sound
                ui.getPage().executeJs("try { var ctx = new (window.AudioContext || window.webkitAudioContext)(); var osc = ctx.createOscillator(); var gain = ctx.createGain(); osc.connect(gain); gain.connect(ctx.destination); osc.type = 'sine'; osc.frequency.value = 600; gain.gain.setValueAtTime(0.1, ctx.currentTime); gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5); osc.start(); osc.stop(ctx.currentTime + 0.5); } catch(e) {}");
            }
            if (currentCount != lastUnreadCount) {
                lastUnreadCount = currentCount;
                updateBadge();
            }
        });
    }

    private void updateBadge() {
        long unreadCount = notificationService.getUnreadCount(currentUser);
        if (unreadCount > 0) {
            badge.setText(String.valueOf(unreadCount));
            badge.setVisible(true);
            bellButton.addClassName("notification-ring");
        } else {
            badge.setVisible(false);
            bellButton.removeClassName("notification-ring");
        }
    }

    private void openNotificationsDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Bildirimlerim");
        dialog.setWidth("400px");
        dialog.setMaxHeight("60vh");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        List<UserNotification> notifications = notificationService.getNotificationsForUser(currentUser);
        if (notifications.isEmpty()) {
            layout.add(new Span("Henüz bir bildiriminiz yok."));
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (UserNotification n : notifications) {
                Div item = new Div();
                item.getStyle()
                        .set("padding", "10px")
                        .set("border-bottom", "1px solid #ccc")
                        .set("width", "100%")
                        .set("background-color", n.isRead() ? "transparent" : "#f0f8ff");

                Span message = new Span(n.getMessage());
                message.getStyle().set("display", "block").set("font-size", "14px");

                Span date = new Span(n.getCreatedAt().format(formatter));
                date.getStyle().set("display", "block").set("font-size", "12px").set("color", "gray");

                item.add(message, date);
                layout.add(item);
            }
        }

        Button markAllReadBtn = new Button("Tümünü Okundu İşaretle", e -> {
            notificationService.markAllAsRead(currentUser);
            updateBadge();
            dialog.close();
            openNotificationsDialog();
        });
        markAllReadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

        dialog.add(layout);
        dialog.getFooter().add(markAllReadBtn);
        dialog.getFooter().add(new Button("Kapat", e -> dialog.close()));
        
        notificationService.markAllAsRead(currentUser);
        updateBadge();

        dialog.open();
    }
}
