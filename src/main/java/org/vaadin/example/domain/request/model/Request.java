package org.vaadin.example.domain.request.model;

import jakarta.persistence.*;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.priority.model.Priority;
import org.vaadin.example.domain.workflow.model.Workflow;
import org.vaadin.example.shared.enums.TaskType;
import java.time.LocalDateTime;
import java.time.LocalDate;
import org.vaadin.example.shared.enums.RequestStatus;

/**
 * A request submitted by a Customer (MUSTERI).
 *
 * SQL: requests_Sila(request_ID, creator_ID, title, description, status, final_priority_score, type, emergency, deadline)
 *
 * Lifecycle (flowchart):
 *  1. Customer submits → PENDING
 *  2. ProductOwner scores → Priority record updated
 *  3. Supervisor scores → final_priority_score set → APPROVED
 *  4. Supervisor assigns developer → Workflow created → ASSIGNED
 *  5. Developer works → IN_PROGRESS → DONE | RESENT
 */
@Entity
@Table(name = "requests_Sila")
public class Request {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_ID")
    private Long id;

    @Column(name = "generated_id", unique = true, length = 6)
    private String generatedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_ID", nullable = false)
    private User creator;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private TaskType type;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    /**
     * Final computed priority score — mirrored from Priority.priorityScore
     * for fast sorting without joining priorities table.
     */
    @Column(name = "final_priority_score")
    private Double finalPriorityScore;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Priority priority;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Workflow workflow;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        generateId();
    }

    public void generateId() {
        if (this.generatedId == null) {
            int randomNum = 100000 + new java.util.Random().nextInt(900000);
            this.generatedId = String.valueOf(randomNum);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Constructors ---

    public Request() {}

    public Request(User creator, String title, String description, TaskType type, LocalDate deadline) {
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.type = type;
        this.deadline = deadline;
    }

    // Legacy constructor just in case
    public Request(User creator, String title, String description) {
        this.creator = creator;
        this.title = title;
        this.description = description;
    }

    // --- Domain logic ---

    /** Returns true if the customer can still edit this request */
    public boolean isEditable() { return status == RequestStatus.PENDING; }

    // --- Getters & Setters ---

    public String getGeneratedId() {
        return generatedId != null ? generatedId : (id != null ? String.format("%06d", id) : null);
    }
    public void setGeneratedId(String generatedId) { this.generatedId = generatedId; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public Double getFinalPriorityScore() { return finalPriorityScore; }
    public void setFinalPriorityScore(Double finalPriorityScore) { this.finalPriorityScore = finalPriorityScore; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Workflow getWorkflow() { return workflow; }
    public void setWorkflow(Workflow workflow) { this.workflow = workflow; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() { return "[#" + id + "] " + title + " (" + status + ")"; }
}
