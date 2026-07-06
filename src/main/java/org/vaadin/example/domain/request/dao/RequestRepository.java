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

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Request r WHERE (r.status IN (org.vaadin.example.shared.enums.RequestStatus.PENDING, org.vaadin.example.shared.enums.RequestStatus.EDITED, org.vaadin.example.shared.enums.RequestStatus.REJECTED)) AND r.priority.productMgrScore IS NULL AND (r.priority.poRejected IS NULL OR r.priority.poRejected = false)")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findPendingForPO();
    
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Request r WHERE (r.status IN (org.vaadin.example.shared.enums.RequestStatus.PENDING, org.vaadin.example.shared.enums.RequestStatus.EDITED, org.vaadin.example.shared.enums.RequestStatus.REJECTED)) AND r.priority.softwareMgrScore IS NULL AND (r.priority.supervisorRejected IS NULL OR r.priority.supervisorRejected = false)")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findPendingForSupervisor();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findByPriority_ProductMgrScoreIsNotNull();
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findByPriority_SoftwareMgrScoreIsNotNull();

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Request r WHERE (r.priority.productMgrScore IS NOT NULL OR r.priority.poRejected = true OR r.status = org.vaadin.example.shared.enums.RequestStatus.UNASSIGNED) AND r.status != org.vaadin.example.shared.enums.RequestStatus.EDITED")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findPastForPO();

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Request r WHERE (r.priority.softwareMgrScore IS NOT NULL OR r.priority.supervisorRejected = true OR r.status = org.vaadin.example.shared.enums.RequestStatus.UNASSIGNED) AND r.status != org.vaadin.example.shared.enums.RequestStatus.EDITED")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findPastForSupervisor();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"creator", "creator.company", "priority"})
    List<Request> findByStatusOrderByFinalPriorityScoreDesc(RequestStatus status);
}
