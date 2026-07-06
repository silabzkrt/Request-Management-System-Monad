package org.vaadin.example.domain.workflow.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaadin.example.domain.developer.model.Developer;
import org.vaadin.example.domain.workflow.model.Workflow;
import org.vaadin.example.shared.enums.WorkStatus;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {

    List<Workflow> findByDeveloper(Developer developer);

    List<Workflow> findByWorkflowStatus(WorkStatus status);

    List<Workflow> findByDeveloper_IdAndWorkflowStatus(Long developerId, WorkStatus status);

    java.util.Optional<Workflow> findByRequest_Id(Long requestId);
}
