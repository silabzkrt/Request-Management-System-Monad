package org.vaadin.example.domain.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.shared.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator"})
    List<Request> findByCreatorOrderByCreatedAtDesc(User creator);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    Optional<Request> findById(Long id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findByStatus(RequestStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Request r LEFT JOIN r.priority p WHERE (r.status IN (org.vaadin.example.shared.enums.RequestStatus.PENDING, org.vaadin.example.shared.enums.RequestStatus.EDITED, org.vaadin.example.shared.enums.RequestStatus.REJECTED)) AND (p IS NULL OR p.productMgrScore IS NULL) AND (p IS NULL OR p.poRejected IS NULL OR p.poRejected = false)")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findPendingForPO();
    
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Request r LEFT JOIN r.priority p WHERE (r.status IN (org.vaadin.example.shared.enums.RequestStatus.PENDING, org.vaadin.example.shared.enums.RequestStatus.EDITED, org.vaadin.example.shared.enums.RequestStatus.REJECTED)) AND (p IS NULL OR p.softwareMgrScore IS NULL) AND (p IS NULL OR p.supervisorRejected IS NULL OR p.supervisorRejected = false)")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findPendingForSupervisor();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findByPriority_ProductMgrScoreIsNotNull();
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findByPriority_SoftwareMgrScoreIsNotNull();

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Request r LEFT JOIN r.priority p WHERE (p IS NOT NULL AND (p.productMgrScore IS NOT NULL OR p.poRejected = true)) OR r.status = org.vaadin.example.shared.enums.RequestStatus.UNASSIGNED")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findPastForPO();

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Request r LEFT JOIN r.priority p WHERE (p IS NOT NULL AND (p.softwareMgrScore IS NOT NULL OR p.supervisorRejected = true)) OR r.status = org.vaadin.example.shared.enums.RequestStatus.UNASSIGNED")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findPastForSupervisor();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findByStatusOrderByFinalPriorityScoreDesc(RequestStatus status);
}
