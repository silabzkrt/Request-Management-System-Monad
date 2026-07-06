package org.vaadin.example.domain.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaadin.example.domain.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"company"})
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    List<User> findByRole(String role);
    List<User> findByRoleAndCompany(String role, org.vaadin.example.domain.company.model.Company company);

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"company"})
    List<User> findAll();
}
