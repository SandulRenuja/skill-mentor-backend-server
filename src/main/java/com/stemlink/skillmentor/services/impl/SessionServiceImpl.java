package com.stemlink.skillmentor.services.impl;

import com.stemlink.skillmentor.dto.ReviewDTO;
import com.stemlink.skillmentor.dto.SessionDTO;
import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.entities.Student;
import com.stemlink.skillmentor.entities.Subject;
import com.stemlink.skillmentor.exceptions.SkillMentorException;
import com.stemlink.skillmentor.respositories.MentorRepository;
import com.stemlink.skillmentor.respositories.SessionRepository;
import com.stemlink.skillmentor.respositories.StudentRepository;
import com.stemlink.skillmentor.respositories.SubjectRepository;
import com.stemlink.skillmentor.security.UserPrincipal;
import com.stemlink.skillmentor.services.SessionService;
import com.stemlink.skillmentor.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final SubjectRepository subjectRepository;
    private final ModelMapper modelMapper;

    private Integer toIntId(Long id) {
        if (id == null) return null;
        return id.intValue();
    }

    // ─── Admin: create session ────────────────────────────────────────────────

    @Override
    public Session createNewSession(SessionDTO sessionDTO) {
        try {
            Student student = studentRepository.findById(sessionDTO.getStudentId())
                    .orElseThrow(() -> new SkillMentorException("Student not found", HttpStatus.NOT_FOUND));

            Mentor mentor = mentorRepository.findByMentorId(String.valueOf(sessionDTO.getMentorId()))
                    .orElseThrow(() -> new SkillMentorException("Mentor not found", HttpStatus.NOT_FOUND));

            Subject subject = subjectRepository.findById(sessionDTO.getSubjectId())
                    .orElseThrow(() -> new SkillMentorException("Subject not found", HttpStatus.NOT_FOUND));

            validateSessionDateTime(sessionDTO.getSessionAt());
            ValidationUtils.validateMentorAvailability(mentor, sessionDTO.getSessionAt(), sessionDTO.getDurationMinutes());
            ValidationUtils.validateStudentAvailability(student, sessionDTO.getSessionAt(), sessionDTO.getDurationMinutes());

            Session session = modelMapper.map(sessionDTO, Session.class);
            session.setStudent(student);
            session.setMentor(mentor);
            session.setSubject(subject);

            return sessionRepository.save(session);
        } catch (SkillMentorException e) {
            log.error("Failed to create session: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating session", e);
            throw new SkillMentorException("Failed to create new session", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─── Student: enroll ─────────────────────────────────────────────────────

    @Override
    public Session enrollSession(UserPrincipal userPrincipal, SessionDTO sessionDTO) {

        // 1. Validate session date is not in the past
        validateSessionDateTime(sessionDTO.getSessionAt());

        // 2. Resolve or auto-create student from JWT
        Student student = studentRepository.findByEmail(userPrincipal.getEmail())
                .orElseGet(() -> {
                    Student s = new Student();
                    s.setStudentId(userPrincipal.getId());
                    s.setEmail(userPrincipal.getEmail());
                    s.setFirstName(userPrincipal.getFirstName());
                    s.setLastName(userPrincipal.getLastName());
                    return studentRepository.save(s);
                });

        // 3. Resolve mentor and subject
        Mentor mentor = mentorRepository.findByMentorId(String.valueOf(sessionDTO.getMentorId()))
                .orElseThrow(() -> new SkillMentorException(
                        "Mentor not found with mentorId: " + sessionDTO.getMentorId(),
                        HttpStatus.NOT_FOUND));

        Subject subject = subjectRepository.findById(sessionDTO.getSubjectId())
                .orElseThrow(() -> new SkillMentorException(
                        "Subject not found with id: " + sessionDTO.getSubjectId(),
                        HttpStatus.NOT_FOUND));

        Integer duration = sessionDTO.getDurationMinutes() != null
                ? sessionDTO.getDurationMinutes() : 60;

        // 4. Double-booking prevention
        ValidationUtils.validateMentorAvailability(mentor, sessionDTO.getSessionAt(), duration);
        ValidationUtils.validateStudentAvailability(student, sessionDTO.getSessionAt(), duration);

        // 5. Duplicate subject booking check — prevent same student booking same subject
        //    at an overlapping time window
        boolean duplicateSubjectBooking = student.getSessions() != null
                && student.getSessions().stream()
                .filter(s -> s.getSubject() != null
                        && s.getSubject().getId().equals(subject.getId()))
                .anyMatch(s -> ValidationUtils.isTimeOverlap(
                        sessionDTO.getSessionAt(),
                        ValidationUtils.addMinutesToDate(sessionDTO.getSessionAt(), duration),
                        s.getSessionAt(),
                        ValidationUtils.addMinutesToDate(s.getSessionAt(),
                                s.getDurationMinutes() != null ? s.getDurationMinutes() : 60)
                ));

        if (duplicateSubjectBooking) {
            throw new SkillMentorException(
                    "You already have a session for this subject at an overlapping time",
                    HttpStatus.CONFLICT);
        }

        // 6. Persist
        Session session = new Session();
        session.setStudent(student);
        session.setMentor(mentor);
        session.setSubject(subject);
        session.setSessionAt(sessionDTO.getSessionAt());
        session.setDurationMinutes(duration);
        session.setSessionStatus("scheduled");
        session.setPaymentStatus("pending");

        return sessionRepository.save(session);
    }

    // ─── Review submission ────────────────────────────────────────────────────

    @Override
    public Session submitReview(Long sessionId, ReviewDTO reviewDTO, UserPrincipal userPrincipal) {
        Session session = sessionRepository.findById(toIntId(sessionId))
                .orElseThrow(() -> new SkillMentorException("Session not found", HttpStatus.NOT_FOUND));

        // Only the student who owns this session (or admin) can review
        if (session.getStudent() == null
                || !session.getStudent().getEmail().equals(userPrincipal.getEmail())) {
            throw new SkillMentorException("You are not authorised to review this session", HttpStatus.FORBIDDEN);
        }

        if (!"completed".equalsIgnoreCase(session.getSessionStatus())) {
            throw new SkillMentorException(
                    "Reviews can only be submitted for completed sessions", HttpStatus.BAD_REQUEST);
        }

        session.setStudentRating(reviewDTO.getRating());
        session.setStudentReview(reviewDTO.getReviewText());
        return sessionRepository.save(session);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Rejects session dates in the past (with 5-minute grace window).
     */
    private void validateSessionDateTime(Date sessionAt) {
        if (sessionAt == null) {
            throw new SkillMentorException("Session date is required", HttpStatus.BAD_REQUEST);
        }
        Date now = new Date();
        // 5-minute grace window
        Date cutoff = new Date(now.getTime() - 5 * 60 * 1000L);
        if (sessionAt.before(cutoff)) {
            throw new SkillMentorException(
                    "Session date cannot be in the past", HttpStatus.BAD_REQUEST);
        }
    }

    // ─── Standard CRUD ───────────────────────────────────────────────────────

    @Override
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Override
    public Session getSessionById(Long id) {
        return sessionRepository.findById(toIntId(id))
                .orElseThrow(() -> new SkillMentorException("Session not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public Session updateSessionById(Long id, SessionDTO updatedSessionDTO) {
        Session session = sessionRepository.findById(toIntId(id))
                .orElseThrow(() -> new SkillMentorException("Session not found", HttpStatus.NOT_FOUND));

        modelMapper.map(updatedSessionDTO, session);

        if (updatedSessionDTO.getStudentId() != null) {
            Student student = studentRepository.findById(updatedSessionDTO.getStudentId())
                    .orElseThrow(() -> new SkillMentorException("Student not found", HttpStatus.NOT_FOUND));
            session.setStudent(student);
        }
        if (updatedSessionDTO.getMentorId() != null) {
            Mentor mentor = mentorRepository.findByMentorId(String.valueOf(updatedSessionDTO.getMentorId()))
                    .orElseThrow(() -> new SkillMentorException("Mentor not found", HttpStatus.NOT_FOUND));
            session.setMentor(mentor);
        }
        if (updatedSessionDTO.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(updatedSessionDTO.getSubjectId())
                    .orElseThrow(() -> new SkillMentorException("Subject not found", HttpStatus.NOT_FOUND));
            session.setSubject(subject);
        }

        return sessionRepository.save(session);
    }

    @Override
    public void deleteSession(Long id) {
        Integer intId = toIntId(id);
        if (!sessionRepository.existsById(intId)) {
            throw new SkillMentorException("Session not found", HttpStatus.NOT_FOUND);
        }
        sessionRepository.deleteById(intId);
    }

    @Override
    public List<Session> getSessionsByStudentEmail(String email) {
        return sessionRepository.findByStudent_Email(email);
    }
}