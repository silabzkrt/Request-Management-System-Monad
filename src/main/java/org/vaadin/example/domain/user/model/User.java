package org.vaadin.example.domain.user.model;

import org.vaadin.example.domain.company.model.Company;

import jakarta.persistence.*;

/**
 * Base class for all users in the system.
 *
 * SQL: users_Sila(user_ID, name_surname, email, role, password, company_ID)
 *
 * CHECK constraint on role:
 *   'ADMIN' | 'YAZILIMCI' | 'MUSTERI' | 'URUN_SORUMLUSU' | 'YAZILIM_YONETICISI'
 *
 * Inheritance: SINGLE_TABLE with discriminator column "user_type".
 * is_approved: supports the admin-approval registration flow from the flowchart.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Table(
    name = "users_Sila",
    uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_ID")
    private Long id;

    @Column(name = "generated_id", unique = true, length = 6)
    private String generatedId;

    @Column(name = "name_surname", nullable = false, length = 50)
    private String nameSurname;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Role string stored in DB — must match the SQL CHECK constraint values:
     * ADMIN | YAZILIMCI | MUSTERI | URUN_SORUMLUSU | YAZILIM_YONETICISI
     */
    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * Flowchart: new users wait on a "pending approval" screen
     * until the admin manually approves their account.
     */
    @Column(name = "is_approved", nullable = false)
    private boolean approved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_ID", nullable = false)
    private Company company;

    @PrePersist
    public void generateId() {
        if (this.generatedId == null) {
            int randomNum = 100000 + new java.util.Random().nextInt(900000);
            this.generatedId = String.valueOf(randomNum);
        }
    }

    // --- Getters & Setters ---

    public String getGeneratedId() {
        return generatedId != null ? generatedId : (id != null ? String.format("%06d", id) : null);
    }
    public void setGeneratedId(String generatedId) { this.generatedId = generatedId; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNameSurname() { return nameSurname; }
    public void setNameSurname(String nameSurname) { this.nameSurname = nameSurname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    @Override
    public String toString() { return nameSurname + " <" + email + "> [" + role + "]"; }
}
