package org.vaadin.example.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.example.domain.user.dao.PasswordResetTokenRepository;
import org.vaadin.example.domain.user.model.PasswordResetToken;
import org.vaadin.example.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, UserService userService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean initiatePasswordReset(String email) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Delete existing token if any, and force flush so insert doesn't fail due to unique constraint
            tokenRepository.findByUser(user).ifPresent(token -> {
                tokenRepository.delete(token);
                tokenRepository.flush();
            });
            
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusHours(1));
            tokenRepository.save(resetToken);
            
            // Construct reset link. Since we don't know the exact base URL dynamically in Vaadin easily without context, we assume localhost:8080 or domain.
            String resetLink = "http://localhost:8080/reset-password?token=" + token;
            
            emailService.sendPasswordResetMail(user.getEmail(), resetLink);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        if (tokenOptional.isPresent()) {
            PasswordResetToken resetToken = tokenOptional.get();
            if (resetToken.getExpiryDate().isAfter(LocalDateTime.now())) {
                return true;
            } else {
                tokenRepository.delete(resetToken);
            }
        }
        return false;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        if (tokenOptional.isPresent()) {
            PasswordResetToken resetToken = tokenOptional.get();
            if (resetToken.getExpiryDate().isAfter(LocalDateTime.now())) {
                User user = resetToken.getUser();
                user.setPassword(passwordEncoder.encode(newPassword));
                userService.save(user); // Actually userService.save handles setting encoded password, but only if ID is null! 
                // Let's use user repository or just let JPA handle it via @Transactional.
                // Wait, UserService.save only encodes if id == null. Since id is NOT null here, we MUST encode it ourselves, which we did.
                // But if we call userService.save(), it will just save it with the encoded password.
                
                tokenRepository.delete(resetToken);
                return true;
            }
        }
        return false;
    }
}
