package org.vaadin.example.domain.contactadmin.model;

import jakarta.persistence.*;
import org.vaadin.example.domain.user.model.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_admin_messages_Sila")
public class ContactAdminMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_ID", nullable = false)
    private User sender;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public ContactAdminMessage() {}

    public ContactAdminMessage(User sender, String title, String description, String attachmentPath) {
        this.sender = sender;
        this.title = title;
        this.description = description;
        this.attachmentPath = attachmentPath;
    }

    public Long getId() { return id; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
