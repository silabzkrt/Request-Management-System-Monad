package org.vaadin.example.domain.workflow.model;

import jakarta.persistence.*;
import org.vaadin.example.domain.developer.model.Developer;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.supervisor.model.Supervisor;
import org.vaadin.example.shared.enums.RequestStatus;
import org.vaadin.example.shared.enums.WorkStatus;
import java.time.LocalDateTime;

/**
 * Represents a developer assignment for a specific Request.
 *
 * SQL: workflows_Sila(task_ID, request_ID, developer_ID, workflow_status)
 *
 * Status lifecycle (SQL CHECK constraint values):
 *   UNASSIGNED → ASSIGNED → RESOLVED
 *                         → RESENT → (back to Supervisor queue → ASSIGNED again)
 */
@Entity
@Table(name = "workflows_Sila")
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_ID", nullable = false, unique = true)
    private Request request;

    /** Null when status is UNASSIGNED or after a RESENT */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_ID")
    private Developer developer;

    /** Supervisor who created this assignment (for audit purposes) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_ID")
    private Supervisor assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_status", nullable = false, length = 20)
    private WorkStatus workflowStatus = WorkStatus.UNASSIGNED;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // --- Constructors ---

    public Workflow() {}

    public Workflow(Request request) {
        this.request = request;
    }

    // --- Domain helper methods ---

    /** Assigns a developer → status: ASSIGNED */
    public void assign(Developer developer, Supervisor assignedBy) {
        this.developer = developer;
        this.assignedBy = assignedBy;
        this.workflowStatus = WorkStatus.ASSIGNED;
        this.assignedAt = LocalDateTime.now();
        if (this.request != null) {
            this.request.setStatus(RequestStatus.ASSIGNED);
        }
    }

    /** Developer marks task done → status: RESOLVED, Request: DONE */
    public void resolve() {
        this.workflowStatus = WorkStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        if (this.request != null) {
            this.request.setStatus(RequestStatus.COMPLETED);
        }
    }

    /** Developer sends back to supervisor → status: RESENT, Request: UNASSIGNED */
    public void resend() {
        this.workflowStatus = WorkStatus.RESENT;
        this.developer = null;
        this.assignedAt = null;
        if (this.request != null) {
            this.request.setStatus(RequestStatus.UNASSIGNED);
        }
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    public Developer getDeveloper() { return developer; }
    public void setDeveloper(Developer developer) { this.developer = developer; }

    public Supervisor getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Supervisor assignedBy) { this.assignedBy = assignedBy; }

    public WorkStatus getWorkflowStatus() { return workflowStatus; }
    public void setWorkflowStatus(WorkStatus workflowStatus) { this.workflowStatus = workflowStatus; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
