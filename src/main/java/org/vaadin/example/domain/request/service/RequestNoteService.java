package org.vaadin.example.domain.request.service;

import org.springframework.stereotype.Service;
import org.vaadin.example.domain.request.dao.RequestNoteRepository;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.request.model.RequestNote;
import org.vaadin.example.domain.user.model.User;

import java.util.List;

@Service
public class RequestNoteService {
    
    private final RequestNoteRepository requestNoteRepository;

    public RequestNoteService(RequestNoteRepository requestNoteRepository) {
        this.requestNoteRepository = requestNoteRepository;
    }

    public List<RequestNote> findNotesByRequest(Request request) {
        return requestNoteRepository.findByRequestOrderByCreatedAtDesc(request);
    }

    public RequestNote addNote(Request request, User submitter, String content) {
        return addNote(request, submitter, content, new java.util.ArrayList<>());
    }

    public RequestNote addNote(Request request, User submitter, String content, List<org.vaadin.example.domain.request.model.NoteAttachment> attachments) {
        RequestNote note = new RequestNote(request, submitter, content);
        if (attachments != null) {
            for (org.vaadin.example.domain.request.model.NoteAttachment att : attachments) {
                att.setNote(note);
                note.getAttachments().add(att);
            }
        }
        return requestNoteRepository.save(note);
    }
}
