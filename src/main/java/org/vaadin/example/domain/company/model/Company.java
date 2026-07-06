package org.vaadin.example.domain.company.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.example.domain.user.model.User;

/**
 * Represents a client company (e.g. a hospital) that uses Monad's software.
 *
 * SQL: company_Sila(company_ID, company_name, company_points)
 *
 * company_points: reflects the company's payment/contract weight.
 * Higher points → higher priority weighting for that company's requests.
 */

 // Make immutable*********************************************************************


@Entity
@Table(name = "company_Sila")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_ID")
    private Long id;

    @Column(name = "company_name", nullable = false, unique = true, length = 100)
    private String companyName;

    @Column(name = "company_points")
    private Double companyPoints;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> employees = new ArrayList<>();

    // --- Constructors ---

    public Company() {}

    public Company(String companyName, Double companyPoints) {
        this.companyName = companyName;
        this.companyPoints = companyPoints;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Double getCompanyPoints() { return companyPoints; }
    public void setCompanyPoints(Double companyPoints) { this.companyPoints = companyPoints; }

    public List<User> getEmployees() { return employees; }
    public void setEmployees(List<User> employees) { this.employees = employees; }

    @Override
    public String toString() { return companyName + " (ID: " + id + ")"; }
}
