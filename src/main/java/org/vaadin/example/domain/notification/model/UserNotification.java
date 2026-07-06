package org.vaadin.example.domain.notification.model;

import jakarta.persistence.*;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications_Sila")
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_ID", nullable = false)
    private User recipient;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_ID", nullable = true)
    private Request relatedRequest;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserNotification() {
    }

    public UserNotification(User recipient, String message, Request relatedRequest) {
        this.recipient = recipient;
        this.message = message;
        this.relatedRequest = relatedRequest;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public Long getId() { return id; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Request getRelatedRequest() { return relatedRequest; }
    public void setRelatedRequest(Request relatedRequest) { this.relatedRequest = relatedRequest; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
