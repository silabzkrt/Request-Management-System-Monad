package org.vaadin.example.domain.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vaadin.example.domain.request.model.Request;
import org.vaadin.example.domain.request.model.RequestNote;

import java.util.List;

@Repository
public interface RequestNoteRepository extends JpaRepository<RequestNote, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"submitter", "attachments"})
    List<RequestNote> findByRequestOrderByCreatedAtDesc(Request request);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"submitter", "attachments"})
    List<RequestNote> findByRequestAndIsInternalFalseOrderByCreatedAtDesc(Request request);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"submitter", "attachments"})
    List<RequestNote> findByRequestAndIsInternalTrueOrderByCreatedAtDesc(Request request);
}
