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
        ContactAdminMessage saved = repository.save(message);
        
        System.out.println("=================================================");
        System.out.println("YENİ İLETİŞİM MESAJI (Email Simülasyonu)");
        System.out.println("Gönderen: " + (saved.getSender() != null ? saved.getSender().getNameSurname() + " (" + saved.getSender().getEmail() + ")" : "Bilinmeyen"));
        System.out.println("Konu: " + saved.getTitle());
        System.out.println("Mesaj:");
        System.out.println(saved.getDescription());
        System.out.println("=================================================");
        
        return saved;
    }

    public java.util.List<ContactAdminMessage> findAll() {
        return repository.findAll();
    }
}
