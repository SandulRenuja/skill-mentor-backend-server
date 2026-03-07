package com.stemlink.skillmentor.respositories;

import com.stemlink.skillmentor.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * NOTE: Session.id is Integer, so JpaRepository<Session, Integer> is correct.
 * The service layer accepts Long IDs and casts where needed, but the actual
 * PK type in the entity is Integer.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
    List<Session> findByStudent_Email(String email);
    Optional<Session> findById(Integer id);
}