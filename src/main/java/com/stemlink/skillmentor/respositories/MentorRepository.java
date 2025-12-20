package com.stemlink.skillmentor.respositories;

import com.stemlink.skillmentor.entities.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
}
