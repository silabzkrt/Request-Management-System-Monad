package org.vaadin.example.domain.priority.model;

import jakarta.persistence.*;
import org.vaadin.example.domain.request.model.Request;

/**
 * Stores the priority scoring details for a single Request.
 *
 * SQL: priorities_Sila(priority_ID, request_ID, company_points, customer_rank,
 *                      product_mgr_score, software_mgr_score, payment_priority, priority_score)
 *
 * Scoring breakdown:
 *  company_points      — snapshot of company_Sila.company_points at submission time
 *  customer_rank       — set by ProductOwner: strategic weight of this customer (per request)
 *  product_mgr_score   — set by ProductOwner (1–5): product scope, urgency, payment impact
 *  software_mgr_score  — set by Supervisor (1–5): technical complexity, feasibility
 *  payment_priority    — payment urgency factor set by ProductOwner
 *  priority_score      — final computed score (by PriorityCalculationService)
 *                         also mirrored on Request.finalPriorityScore
 *
 * DB CHECK constraints: product_mgr_score BETWEEN 1 AND 5
 *                       software_mgr_score BETWEEN 1 AND 5
 */
@Entity
@Table(name = "priorities_Sila")
public class Priority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "priority_ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_ID", nullable = false, unique = true)
    private Request request;

    @Column(name = "company_points")
    private Double companyPoints;

    @Column(name = "customer_rank")
    private Integer customerRank;

    @Column(name = "product_mgr_score")
    private Integer productMgrScore;

    @Column(name = "software_mgr_score")
    private Integer softwareMgrScore;

    @Column(name = "payment_priority")
    private Double paymentPriority;

    @Column(name = "priority_score")
    private Double priorityScore;

    @Column(name = "po_rejected", columnDefinition = "boolean default false")
    private Boolean poRejected = false;

    @Column(name = "supervisor_rejected", columnDefinition = "boolean default false")
    private Boolean supervisorRejected = false;

    @Column(name = "highest_priority", columnDefinition = "boolean default false")
    private Boolean highestPriority = false;

    // --- Constructors ---

    public Priority() {}

    public Priority(Request request, Double companyPoints) {
        this.request = request;
        this.companyPoints = companyPoints;
    }

    // --- Domain logic ---

    /** Returns true when both manager scores are present and final score can be calculated. */
    public boolean isReadyForFinalScore() {
        return productMgrScore != null && softwareMgrScore != null;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }

    public Double getCompanyPoints() { return companyPoints; }
    public void setCompanyPoints(Double companyPoints) { this.companyPoints = companyPoints; }

    public Integer getCustomerRank() { return customerRank; }
    public void setCustomerRank(Integer customerRank) { this.customerRank = customerRank; }

    public Integer getProductMgrScore() { return productMgrScore; }
    public void setProductMgrScore(Integer productMgrScore) { this.productMgrScore = productMgrScore; }

    public Integer getSoftwareMgrScore() { return softwareMgrScore; }
    public void setSoftwareMgrScore(Integer softwareMgrScore) { this.softwareMgrScore = softwareMgrScore; }

    public Double getPaymentPriority() { return paymentPriority; }
    public void setPaymentPriority(Double paymentPriority) { this.paymentPriority = paymentPriority; }

    public Double getPriorityScore() { return priorityScore; }
    public void setPriorityScore(Double priorityScore) { this.priorityScore = priorityScore; }

    public Boolean getPoRejected() { return poRejected; }
    public void setPoRejected(Boolean poRejected) { this.poRejected = poRejected; }

    public Boolean getSupervisorRejected() { return supervisorRejected; }
    public void setSupervisorRejected(Boolean supervisorRejected) { this.supervisorRejected = supervisorRejected; }

    public Boolean getHighestPriority() { return highestPriority; }
    public void setHighestPriority(Boolean highestPriority) { this.highestPriority = highestPriority; }
}
