package org.vaadin.example.domain.developer.model;

import jakarta.persistence.*;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.workflow.model.Workflow;
import java.util.ArrayList;
import java.util.List;

/**
 * YAZILIMCI (Developer) — works on requests assigned by the Software Manager.
 *
 * Responsibilities (flowchart):
 *  - Views assigned tasks sorted by pre-calculated priority score (highest first)
 *  - Updates task status: ASSIGNED → IN_PROGRESS → RESOLVED | RESENT
 */
@Entity
@DiscriminatorValue("YAZILIMCI")
public class Developer extends User {

    @OneToMany(mappedBy = "developer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Workflow> assignedWorkflows = new ArrayList<>();

    public Developer() {
        setRole("YAZILIMCI");
    }

    public List<Workflow> getAssignedWorkflows() { return assignedWorkflows; }
    public void setAssignedWorkflows(List<Workflow> assignedWorkflows) {
        this.assignedWorkflows = assignedWorkflows;
    }
}
