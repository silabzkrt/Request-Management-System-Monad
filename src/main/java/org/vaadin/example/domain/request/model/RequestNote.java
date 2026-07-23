package org.vaadin.example.domain.request.model;

import jakarta.persistence.*;
import org.vaadin.example.domain.user.model.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_notes_Sila")
public class RequestNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_ID", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_ID", nullable = false)
    private User submitter;

    @Column(name = "content", nullable = false, length = 4000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private java.util.List<NoteAttachment> attachments = new java.util.ArrayList<>();

    @Column(name = "is_internal", columnDefinition = "boolean default false")
    private boolean isInternal = false;

    public RequestNote() {
    }

    public RequestNote(Request request, User submitter, String content, boolean isInternal) {
        this.request = request;
        this.submitter = submitter;
        this.content = content;
        this.isInternal = isInternal;
        this.createdAt = LocalDateTime.now();
    }

    public RequestNote(Request request, User submitter, String content) {
        this(request, submitter, content, false);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public User getSubmitter() {
        return submitter;
    }

    public void setSubmitter(User submitter) {
        this.submitter = submitter;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.util.List<NoteAttachment> getAttachments() { return attachments; }
    public void setAttachments(java.util.List<NoteAttachment> attachments) { this.attachments = attachments; }

    public boolean isInternal() { return isInternal; }
    public void setInternal(boolean internal) { isInternal = internal; }
}
