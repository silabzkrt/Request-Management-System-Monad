package org.vaadin.example.domain.contactadmin.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaadin.example.domain.contactadmin.model.ContactAdminMessage;

@Repository
public interface ContactAdminMessageRepository extends JpaRepository<ContactAdminMessage, Long> {
}
