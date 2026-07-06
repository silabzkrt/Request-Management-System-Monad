package org.vaadin.example.domain.workflow.service;

import org.springframework.stereotype.Service;
import org.vaadin.example.domain.developer.model.Developer;
import org.vaadin.example.domain.notification.service.UserNotificationService;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.supervisor.model.Supervisor;
import org.vaadin.example.domain.request.dao.RequestRepository;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.vaadin.example.domain.workflow.dao.WorkflowRepository;
import org.vaadin.example.domain.workflow.model.Workflow;
import org.vaadin.example.shared.enums.UserTypes;
import org.vaadin.example.shared.enums.WorkStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Workflow (task assignment) entities.
 */
@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final UserNotificationService userNotificationService;
    private final UserService userService;
    private final RequestRepository requestRepository;

    public WorkflowService(WorkflowRepository workflowRepository,
                           UserNotificationService userNotificationService,
                           UserService userService,
                           RequestRepository requestRepository) {
        this.workflowRepository = workflowRepository;
        this.userNotificationService = userNotificationService;
        this.userService = userService;
        this.requestRepository = requestRepository;
    }

    public List<Workflow> findAll() {
        return workflowRepository.findAll();
    }

    public Optional<Workflow> findById(Long id) {
        return workflowRepository.findById(id);
    }

    public Optional<Workflow> findByRequestId(Long requestId) {
        return workflowRepository.findByRequest_Id(requestId);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Workflow> findActiveByDeveloper(Developer developer) {
        List<Workflow> workflows = workflowRepository.findByDeveloper_IdAndWorkflowStatus(developer.getId(), WorkStatus.ASSIGNED);
        for (Workflow w : workflows) {
            if (w.getRequest() != null) {
                // Trigger lazy loading
                if (w.getRequest().getCreator() != null && w.getRequest().getCreator().getCompany() != null) {
                    w.getRequest().getCreator().getCompany().getCompanyName();
                }
                if (w.getRequest().getPriority() != null) {
                    w.getRequest().getPriority().getProductMgrScore();
                }
            }
        }
        workflows.sort((w1, w2) -> {
            Double s1 = w1.getRequest() != null && w1.getRequest().getFinalPriorityScore() != null ? w1.getRequest().getFinalPriorityScore() : -1.0;
            Double s2 = w2.getRequest() != null && w2.getRequest().getFinalPriorityScore() != null ? w2.getRequest().getFinalPriorityScore() : -1.0;
            return s2.compareTo(s1);
        });
        return workflows;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Workflow> findResolvedByDeveloper(Developer developer) {
        List<Workflow> workflows = workflowRepository.findByDeveloper_IdAndWorkflowStatus(developer.getId(), WorkStatus.RESOLVED);
        for (Workflow w : workflows) {
            if (w.getRequest() != null) {
                if (w.getRequest().getCreator() != null && w.getRequest().getCreator().getCompany() != null) {
                    w.getRequest().getCreator().getCompany().getCompanyName();
                }
                if (w.getRequest().getPriority() != null) {
                    w.getRequest().getPriority().getProductMgrScore();
                }
            }
        }
        workflows.sort((w1, w2) -> {
            Double s1 = w1.getRequest() != null && w1.getRequest().getFinalPriorityScore() != null ? w1.getRequest().getFinalPriorityScore() : -1.0;
            Double s2 = w2.getRequest() != null && w2.getRequest().getFinalPriorityScore() != null ? w2.getRequest().getFinalPriorityScore() : -1.0;
            return s2.compareTo(s1);
        });
        return workflows;
    }

    public List<Workflow> findByStatus(WorkStatus status) {
        return workflowRepository.findByWorkflowStatus(status);
    }

    /**
     * Creates a new UNASSIGNED workflow for an approved request.
     */
    public Workflow createForRequest(Request request) {
        Workflow workflow = new Workflow(request);
        request.setWorkflow(workflow);
        return workflowRepository.save(workflow);
    }

    /**
     * Creates and directly assigns a workflow for a request.
     */
    @org.springframework.transaction.annotation.Transactional
    public Workflow createAndAssignForRequest(Request request, Developer developer, Supervisor supervisor) {
        request = requestRepository.findById(request.getId()).orElse(request);
        Workflow workflow = new Workflow(request);
        workflow.assign(developer, supervisor);
        workflow = workflowRepository.save(workflow);
        request.setWorkflow(workflow);
        userNotificationService.notifySubmitter(request, "yazılımcıya atandı");
        userNotificationService.sendNotification(developer, "Size yeni bir talep atandı: " + request.getTitle(), request);
        return workflow;
    }

    /**
     * Assigns a developer to a workflow.
     */
    @org.springframework.transaction.annotation.Transactional
    public Workflow assign(Workflow workflow, Developer developer, Supervisor assignedBy) {
        workflow = workflowRepository.findById(workflow.getId()).orElse(workflow);
        workflow.assign(developer, assignedBy);
        if (workflow.getRequest() != null) {
            requestRepository.save(workflow.getRequest());
        }
        userNotificationService.notifySubmitter(workflow.getRequest(), "yazılım yönetici tarafından atandı");
        userNotificationService.sendNotification(developer, "Size yeni bir talep atandı: " + workflow.getRequest().getTitle(), workflow.getRequest());
        return workflowRepository.save(workflow);
    }

    /**
     * Developer marks the task as RESOLVED.
     */
    @org.springframework.transaction.annotation.Transactional
    public Workflow resolve(Workflow workflow) {
        workflow = workflowRepository.findById(workflow.getId()).orElse(workflow);
        workflow.resolve();
        userNotificationService.notifySubmitter(workflow.getRequest(), "tamamlandı");
        
        List<User> supers = userService.findByRole(UserTypes.YAZILIM_YONETICISI.getSpringRole());
        userNotificationService.notifyUsersByRole(supers, "ID: " + workflow.getRequest().getId() + " nolu talep geliştirici tarafından tamamlandı.", workflow.getRequest());
        
        if (workflow.getRequest().getCreator() != null && workflow.getRequest().getCreator().getCompany() != null) {
            List<User> pos = userService.findByRoleAndCompany(UserTypes.URUN_SORUMLUSU.getSpringRole(), workflow.getRequest().getCreator().getCompany());
            userNotificationService.notifyUsersByRole(pos, "ID: " + workflow.getRequest().getId() + " nolu talep geliştirici tarafından tamamlandı.", workflow.getRequest());
        }
        if (workflow.getRequest() != null) {
            requestRepository.save(workflow.getRequest());
        }
        
        return workflowRepository.save(workflow);
    }

    /**
     * Developer sends the task back to the supervisor (RESENT).
     */
    @org.springframework.transaction.annotation.Transactional
    public Workflow resend(Workflow workflow) {
        workflow = workflowRepository.findById(workflow.getId()).orElse(workflow);
        workflow.resend();
        if (workflow.getRequest() != null) {
            requestRepository.save(workflow.getRequest());
        }
        List<User> supers = userService.findByRole(UserTypes.YAZILIM_YONETICISI.getSpringRole());
        userNotificationService.notifyUsersByRole(supers, "ID: " + workflow.getRequest().getId() + " nolu talep geliştirici tarafından geri gönderildi (RESENT).", workflow.getRequest());
        return workflowRepository.save(workflow);
    }

    public void delete(Long id) {
        workflowRepository.deleteById(id);
    }
}
