package org.vaadin.example.domain.customer.model;

import jakarta.persistence.*;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.request.model.Request;
import java.util.ArrayList;
import java.util.List;

/**
 * MUSTERI (Customer) — hospital IT staff who submit software requests.
 *
 * Responsibilities (flowchart):
 *  - Create new requests (title + description) → status starts as PENDING
 *  - View a list of their own past requests
 *  - Edit/update their own PENDING requests
 *  - Contact admin about system errors
 */
@Entity
@DiscriminatorValue("MUSTERI")
public class Customer extends User {

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Request> submittedRequests = new ArrayList<>();

    public Customer() {
        setRole("MUSTERI");
    }

    public List<Request> getSubmittedRequests() { return submittedRequests; }
    public void setSubmittedRequests(List<Request> submittedRequests) {
        this.submittedRequests = submittedRequests;
    }
}
