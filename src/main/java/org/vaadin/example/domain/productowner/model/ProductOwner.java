package org.vaadin.example.domain.productowner.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.vaadin.example.domain.user.model.User;

/**
 * URUN_SORUMLUSU (Product Owner) — evaluates incoming requests
 * from a product perspective and contributes to priority scoring.
 *
 * Responsibilities (flowchart):
 *  - Views requests waiting for product evaluation
 *  - Scores each request:
 *      • product_mgr_score (1–5): urgency, product scope, payment impact
 *      • customer_rank: strategic weight of the submitting customer
 *      • payment_priority: payment-based urgency factor
 *  - Views past requests including downstream scores
 */
@Entity
@DiscriminatorValue("URUN_SORUMLUSU")
public class ProductOwner extends User {

    public ProductOwner() {
        setRole("URUN_SORUMLUSU");
    }
}
