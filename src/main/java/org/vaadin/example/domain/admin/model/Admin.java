package org.vaadin.example.domain.admin.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.vaadin.example.domain.user.model.User;

/**
 * ADMIN — full system access.
 *
 * Responsibilities (flowchart):
 *  - Approves or rejects newly registered users
 *  - Manages all existing users (view, edit, deactivate)
 *  - Receives system-error reports from customers
 *  - Views all requests across all companies
 */
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    public Admin() {
        setRole("ADMIN");
        setApproved(true); // Admins are always pre-approved
    }
}
