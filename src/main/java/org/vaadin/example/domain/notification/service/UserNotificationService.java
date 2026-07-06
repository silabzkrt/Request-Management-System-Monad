package org.vaadin.example.domain.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.example.domain.notification.dao.UserNotificationRepository;
import org.vaadin.example.domain.notification.model.UserNotification;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.user.model.User;

import java.util.List;

@Service
public class UserNotificationService {

    private final UserNotificationRepository repository;

    public UserNotificationService(UserNotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void sendNotification(User recipient, String message, Request relatedRequest) {
        if (recipient == null) return;
        UserNotification notification = new UserNotification(recipient, message, relatedRequest);
        repository.save(notification);
    }

    @Transactional
    public void notifySubmitter(Request request, String actionStr) {
        if (request == null || request.getCreator() == null) return;
        String message = request.getCreatedAt().toLocalDate().toString() + " tarihinde ilettiğiniz '" + 
                         request.getTitle() + "' başlıklı talebiniz " + actionStr + "!";
        sendNotification(request.getCreator(), message, request);
    }

    @Transactional
    public void notifyUsersByRole(List<User> users, String message, Request relatedRequest) {
        if (users == null || users.isEmpty()) return;
        for (User user : users) {
            sendNotification(user, message, relatedRequest);
        }
    }

    public List<UserNotification> getNotificationsForUser(User user) {
        return repository.findByRecipientOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {
        return repository.countByRecipientAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(UserNotification notification) {
        notification.setRead(true);
        repository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead(User user) {
        List<UserNotification> unread = repository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .filter(n -> !n.isRead())
                .toList();
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
    }
}
