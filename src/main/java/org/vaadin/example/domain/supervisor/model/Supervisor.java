package org.vaadin.example.domain.supervisor.model;

import jakarta.persistence.*;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.workflow.model.Workflow;
import java.util.ArrayList;
import java.util.List;

/**
 * YAZILIM_YONETICISI (Software Manager / Supervisor).
 *
 * Responsibilities (flowchart):
 *  - Views requests forwarded from the Product Owner's queue
 *  - Adds software_mgr_score (1–5) → triggers final priority calculation
 *  - Assigns scored requests to developers (drag-and-drop → Workflow record created)
 *  - Views current developer workloads
 *  - Views past requests and their outcomes
 */
@Entity
@DiscriminatorValue("YAZILIM_YONETICISI")
public class Supervisor extends User {

    @OneToMany(mappedBy = "assignedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Workflow> managedWorkflows = new ArrayList<>();

    public Supervisor() {
        setRole("YAZILIM_YONETICISI");
    }

    public List<Workflow> getManagedWorkflows() { return managedWorkflows; }
    public void setManagedWorkflows(List<Workflow> managedWorkflows) {
        this.managedWorkflows = managedWorkflows;
    }
}
