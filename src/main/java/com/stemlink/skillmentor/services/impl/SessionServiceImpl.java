package com.stemlink.skillmentor.services.impl;

import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.entities.Student;
import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Subject;
import com.stemlink.skillmentor.respositories.SessionRepository;
import com.stemlink.skillmentor.respositories.StudentRepository;
import com.stemlink.skillmentor.respositories.MentorRepository;
import com.stemlink.skillmentor.respositories.SubjectRepository;
import com.stemlink.skillmentor.dto.SessionDTO;
import com.stemlink.skillmentor.services.SessionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final SubjectRepository subjectRepository;
    private final ModelMapper modelMapper;

    public Session createNewSession(SessionDTO sessionDTO) {
        // Fetch the related entities by their IDs
        Student student = studentRepository.findById(sessionDTO.getStudentId()).get();
        Mentor mentor = mentorRepository.findById(sessionDTO.getMentorId()).get();
        Subject subject = subjectRepository.findById(sessionDTO.getSubjectId()).get();

        // Create and populate the Session entity
        Session session = new Session();
        session.setStudent(student);
        session.setMentor(mentor);
        session.setSubject(subject);
        session.setSessionAt(sessionDTO.getSessionAt());
        session.setDurationMinutes(sessionDTO.getDurationMinutes());
        session.setSessionStatus(sessionDTO.getSessionStatus());
        session.setMeetingLink(sessionDTO.getMeetingLink());
        session.setSessionNotes(sessionDTO.getSessionNotes());
        session.setStudentReview(sessionDTO.getStudentReview());
        session.setStudentRating(sessionDTO.getStudentRating());

        return sessionRepository.save(session);
    }

    public List<Session> getAllSessions() {
        return sessionRepository.findAll(); // SELECT * FROM sessions
    }

    public Session getSessionById(Long id) {
        return sessionRepository.findById(id).get();
    }

    public Session updateSessionById(Long id, SessionDTO updatedSessionDTO) {
        Session session = sessionRepository.findById(id).get();

        // Update the related entities
        if (updatedSessionDTO.getStudentId() != null) {
            Student student = studentRepository.findById(updatedSessionDTO.getStudentId()).get();
            session.setStudent(student);
        }
        if (updatedSessionDTO.getMentorId() != null) {
            Mentor mentor = mentorRepository.findById(updatedSessionDTO.getMentorId()).get();
            session.setMentor(mentor);
        }
        if (updatedSessionDTO.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(updatedSessionDTO.getSubjectId()).get();
            session.setSubject(subject);
        }

        // Update other fields
        if (updatedSessionDTO.getSessionAt() != null) {
            session.setSessionAt(updatedSessionDTO.getSessionAt());
        }
        if (updatedSessionDTO.getDurationMinutes() != null) {
            session.setDurationMinutes(updatedSessionDTO.getDurationMinutes());
        }
        if (updatedSessionDTO.getSessionStatus() != null) {
            session.setSessionStatus(updatedSessionDTO.getSessionStatus());
        }
        if (updatedSessionDTO.getMeetingLink() != null) {
            session.setMeetingLink(updatedSessionDTO.getMeetingLink());
        }
        if (updatedSessionDTO.getSessionNotes() != null) {
            session.setSessionNotes(updatedSessionDTO.getSessionNotes());
        }
        if (updatedSessionDTO.getStudentReview() != null) {
            session.setStudentReview(updatedSessionDTO.getStudentReview());
        }
        if (updatedSessionDTO.getStudentRating() != null) {
            session.setStudentRating(updatedSessionDTO.getStudentRating());
        }

        return sessionRepository.save(session);
    }

    public void deleteSession(Long id) {
        sessionRepository.deleteById(id);
    }
}
