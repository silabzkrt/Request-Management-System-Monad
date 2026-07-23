package org.vaadin.example.domain.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vaadin.example.domain.user.model.PasswordResetToken;

import org.vaadin.example.domain.user.model.User;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);
    void deleteByToken(String token);
}
