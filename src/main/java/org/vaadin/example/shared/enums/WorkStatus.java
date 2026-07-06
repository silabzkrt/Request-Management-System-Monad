package org.vaadin.example.shared.enums;

/**
 * Workflow (task assignment) status values.
 *
 * Matches the SQL CHECK constraint on workflows_Sila.workflow_status:
 *   CHECK (workflow_status IN ('ASSIGNED', 'UNASSIGNED', 'RESOLVED', 'RESENT'))
 *
 * Lifecycle:
 *   UNASSIGNED → ASSIGNED → RESOLVED
 *                         → RESENT → (back to Supervisor queue → ASSIGNED again)
 */
public enum WorkStatus {
    PENDING,
    /** Request approved and scored but no developer assigned yet */
    UNASSIGNED,

    /** Supervisor assigned a developer via drag-and-drop */
    ASSIGNED,

    /** Developer completed the task */
    RESOLVED,

    /** Developer sent the request back to the Supervisor for clarification */
    RESENT;

    /** Returns a human-readable label */
    public String getLabel() {
        return switch (this) {
            case PENDING   -> "Pending";
            case UNASSIGNED -> "Unassigned";
            case ASSIGNED   -> "Assigned";
            case RESOLVED   -> "Resolved";
            case RESENT     -> "Resent to Supervisor";
        };
    }
}
