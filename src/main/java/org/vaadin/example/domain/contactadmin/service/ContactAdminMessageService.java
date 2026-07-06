package org.vaadin.example.domain.contactadmin.service;

import org.springframework.stereotype.Service;
import org.vaadin.example.domain.contactadmin.dao.ContactAdminMessageRepository;
import org.vaadin.example.domain.contactadmin.model.ContactAdminMessage;

@Service
public class ContactAdminMessageService {

    private final ContactAdminMessageRepository repository;

    public ContactAdminMessageService(ContactAdminMessageRepository repository) {
        this.repository = repository;
    }

    public ContactAdminMessage save(ContactAdminMessage message) {
        return repository.save(message);
    }
}
