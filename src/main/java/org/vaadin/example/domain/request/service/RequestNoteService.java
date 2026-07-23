package org.vaadin.example.domain.request.service;

import org.springframework.stereotype.Service;
import org.vaadin.example.domain.notification.service.UserNotificationService;
import org.vaadin.example.domain.request.dao.RequestNoteRepository;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.request.model.RequestNote;
import org.vaadin.example.domain.user.model.User;
import org.vaadin.example.domain.user.service.UserService;
import org.vaadin.example.shared.enums.UserTypes;

import java.util.List;

@Service
public class RequestNoteService {
    
    private final RequestNoteRepository requestNoteRepository;
    private final UserNotificationService userNotificationService;
    private final UserService userService;

    public RequestNoteService(RequestNoteRepository requestNoteRepository,
                              UserNotificationService userNotificationService,
                              UserService userService) {
        this.requestNoteRepository = requestNoteRepository;
        this.userNotificationService = userNotificationService;
        this.userService = userService;
    }

    public List<RequestNote> findNotesByRequest(Request request, Boolean isInternal) {
        if (isInternal == null) {
            return requestNoteRepository.findByRequestOrderByCreatedAtDesc(request);
        } else if (isInternal) {
            return requestNoteRepository.findByRequestAndIsInternalTrueOrderByCreatedAtDesc(request);
        } else {
            return requestNoteRepository.findByRequestAndIsInternalFalseOrderByCreatedAtDesc(request);
        }
    }

    public RequestNote addNote(Request request, User submitter, String content, boolean isInternal) {
        return addNote(request, submitter, content, new java.util.ArrayList<>(), isInternal);
    }

    public RequestNote addNote(Request request, User submitter, String content, List<org.vaadin.example.domain.request.model.NoteAttachment> attachments, boolean isInternal) {
        RequestNote note = new RequestNote(request, submitter, content, isInternal);
        if (attachments != null) {
            for (org.vaadin.example.domain.request.model.NoteAttachment att : attachments) {
                att.setNote(note);
                note.getAttachments().add(att);
            }
        }
        
        RequestNote savedNote = requestNoteRepository.save(note);
        
        // Notify related users
        String notifyMsg = (request.getGeneratedId() != null ? request.getGeneratedId() : "Yeni") + " numaralı talebe " + submitter.getNameSurname() + " tarafından yeni bir mesaj eklendi.";
        
        // 1. Notify Customer (if submitter is not the customer and note is not internal)
        if (!isInternal && request.getCreator() != null && !request.getCreator().getId().equals(submitter.getId())) {
            userNotificationService.sendNotification(request.getCreator(), notifyMsg, request);
        }
        
        // 2. Notify Assigned Developer (if any and not submitter)
        if (request.getWorkflow() != null && request.getWorkflow().getDeveloper() != null) {
            if (!request.getWorkflow().getDeveloper().getId().equals(submitter.getId())) {
                userNotificationService.sendNotification(request.getWorkflow().getDeveloper(), notifyMsg, request);
            }
        }
        
        // 3. Notify Product Owners
        List<User> productOwners = userService.findByRole(UserTypes.URUN_SORUMLUSU.getSpringRole());
        for (User po : productOwners) {
            if (!po.getId().equals(submitter.getId())) {
                userNotificationService.sendNotification(po, notifyMsg, request);
            }
        }
        
        // 4. Notify Supervisors
        List<User> supervisors = userService.findByRole(UserTypes.YAZILIM_YONETICISI.getSpringRole());
        for (User sup : supervisors) {
            if (!sup.getId().equals(submitter.getId())) {
                userNotificationService.sendNotification(sup, notifyMsg, request);
            }
        }
        
        return savedNote;
    }
}
