package org.vaadin.example.domain.notification.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaadin.example.domain.notification.model.UserNotification;
import org.vaadin.example.domain.user.model.User;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"relatedRequest"})
    List<UserNotification> findByRecipientOrderByCreatedAtDesc(User recipient);
    
    long countByRecipientAndIsReadFalse(User recipient);
}
