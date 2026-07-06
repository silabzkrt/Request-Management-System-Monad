package org.vaadin.example.domain.request.service;

import org.springframework.stereotype.Service;
import org.vaadin.example.domain.notification.service.UserNotificationService;
import org.vaadin.example.domain.priority.model.Priority;
import org.vaadin.example.domain.priority.service.PriorityCalculationService;
import org.vaadin.example.domain.request.dao.RequestRepository;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.vaadin.example.shared.enums.RequestStatus;
import org.vaadin.example.shared.enums.UserTypes;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Request (talep) entities.
 * Automatically initializes a Priority record when a new request is saved.
 */
@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final PriorityCalculationService priorityCalculationService;
    private final UserNotificationService userNotificationService;
    private final UserService userService;

    public RequestService(RequestRepository requestRepository,
                          PriorityCalculationService priorityCalculationService,
                          UserNotificationService userNotificationService,
                          UserService userService) {
        this.requestRepository = requestRepository;
        this.priorityCalculationService = priorityCalculationService;
        this.userNotificationService = userNotificationService;
        this.userService = userService;
    }

    public List<Request> findAll() {
        return requestRepository.findAll();
    }

    public Optional<Request> findById(Long id) {
        return requestRepository.findById(id);
    }

    public List<Request> findByCreator(User creator) {
        return requestRepository.findByCreatorOrderByCreatedAtDesc(creator);
    }

    public Optional<Request> findByIdWithDetails(Long id) {
        return requestRepository.findById(id);
    }

    public List<Request> findByStatus(RequestStatus status) {
        return requestRepository.findByStatus(status);
    }

    public List<Request> findPendingForPO() {
        return requestRepository.findPendingForPO();
    }

    public List<Request> findPendingForSupervisor() {
        return requestRepository.findPendingForSupervisor();
    }

    public List<Request> findPastForPO() {
        return requestRepository.findPastForPO();
    }

    public List<Request> findPastForSupervisor() {
        return requestRepository.findPastForSupervisor();
    }

    /**
     * Saves a request and creates an empty Priority record on first save.
     */
    public Request save(Request request) {
        if (request.getId() == null && request.getPriority() == null) {
            Priority priority = new Priority(request,
                request.getCreator() != null && request.getCreator().getCompany() != null
                    ? request.getCreator().getCompany().getCompanyPoints()
                    : null);
            request.setPriority(priority);
        }
        return requestRepository.save(request);
    }
    
    public void rejectBySupervisor(Request req) {
        req.setStatus(RequestStatus.REJECTED);
        if (req.getPriority() != null) {
            req.getPriority().setSupervisorRejected(true);
        }
        save(req);
        userNotificationService.notifySubmitter(req, "Yazılım Yöneticisi tarafından reddedildi");
        if (req.getCreator() != null && req.getCreator().getCompany() != null) {
            List<User> pos = userService.findByRoleAndCompany(UserTypes.URUN_SORUMLUSU.getSpringRole(), req.getCreator().getCompany());
            userNotificationService.notifyUsersByRole(pos, "ID: " + req.getId() + " nolu talep Yazılım Yöneticisi tarafından reddedildi.", req);
        }
    }

    public void rejectByPO(Request req) {
        req.setStatus(RequestStatus.REJECTED);
        if (req.getPriority() != null) {
            req.getPriority().setPoRejected(true);
        }
        save(req);
        userNotificationService.notifySubmitter(req, "Ürün Sorumlusu tarafından reddedildi");
        if (req.getCreator() != null && req.getCreator().getCompany() != null) {
            List<User> supers = userService.findByRoleAndCompany(UserTypes.YAZILIM_YONETICISI.getSpringRole(), req.getCreator().getCompany());
            userNotificationService.notifyUsersByRole(supers, "ID: " + req.getId() + " nolu talep Ürün Sorumlusu tarafından reddedildi.", req);
        }
    }

    public void delete(Long id) {
        requestRepository.deleteById(id);
    }
}
