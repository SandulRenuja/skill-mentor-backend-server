package com.stemlink.skillmentor.services.impl;

import com.stemlink.skillmentor.dto.response.MentorProfileResponseDTO;
import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.entities.Subject;
import com.stemlink.skillmentor.exceptions.SkillMentorException;
import com.stemlink.skillmentor.respositories.MentorRepository;
import com.stemlink.skillmentor.respositories.SessionRepository;
import com.stemlink.skillmentor.services.MentorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorServiceImpl implements MentorService {

    private final MentorRepository mentorRepository;
    private final SessionRepository sessionRepository;
    private final ModelMapper modelMapper;

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @CacheEvict(value = "mentors", allEntries = true)
    public Mentor createNewMentor(Mentor mentor) {
        try {
            return mentorRepository.save(mentor);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating mentor: {}", e.getMessage());
            throw new SkillMentorException("Mentor with this email already exists", HttpStatus.CONFLICT);
        } catch (Exception exception) {
            log.error("Failed to create new mentor", exception);
            throw new SkillMentorException("Failed to create new mentor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Cacheable(value = "mentors", key = "(#name ?: '') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Mentor> getAllMentors(String name, Pageable pageable) {
        try {
            log.debug("getting mentors with name: {}", name);
            if (name != null && !name.isEmpty()) {
                return mentorRepository.findByName(name, pageable);
            }
            return mentorRepository.findAll(pageable);
        } catch (Exception exception) {
            log.error("Failed to get all mentors", exception);
            throw new SkillMentorException("Failed to get all mentors", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Cacheable(value = "mentors", key = "#id")
    public Mentor getMentorById(Long id) {
        try {
            Mentor mentor = mentorRepository.findById(id).orElseThrow(
                    () -> new SkillMentorException("Mentor Not found", HttpStatus.NOT_FOUND)
            );
            log.info("Successfully fetched mentor {}", id);
            return mentor;
        } catch (SkillMentorException skillMentorException) {
            log.warn("Mentor not found with id: {} to fetch", id, skillMentorException);
            throw new SkillMentorException("Mentor Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error getting mentor", exception);
            throw new SkillMentorException("Failed to get mentor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Builds a rich MentorProfileResponseDTO with:
     *  - per-subject enrollment counts (sessions linked to that subject)
     *  - average rating and review count from all session reviews
     *  - recent reviews (last 10 sessions with a studentReview)
     */
    @Override
    public MentorProfileResponseDTO getMentorProfile(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new SkillMentorException("Mentor not found", HttpStatus.NOT_FOUND));

        MentorProfileResponseDTO dto = new MentorProfileResponseDTO();

        // Map basic fields
        dto.setId(mentor.getId());
        dto.setMentorId(mentor.getMentorId());
        dto.setFirstName(mentor.getFirstName());
        dto.setLastName(mentor.getLastName());
        dto.setEmail(mentor.getEmail());
        dto.setPhoneNumber(mentor.getPhoneNumber());
        dto.setTitle(mentor.getTitle());
        dto.setProfession(mentor.getProfession());
        dto.setCompany(mentor.getCompany());
        dto.setExperienceYears(mentor.getExperienceYears());
        dto.setBio(mentor.getBio());
        dto.setProfileImageUrl(mentor.getProfileImageUrl());
        dto.setIsCertified(mentor.getIsCertified());
        dto.setStartYear(mentor.getStartYear());
        dto.setPositiveReviews(mentor.getPositiveReviews());

        // Fetch all sessions for this mentor
        List<Session> mentorSessions = mentor.getSessions() != null
                ? mentor.getSessions()
                : List.of();

        // ── Compute total enrollments ─────────────────────────────────────
        int totalEnrollments = mentorSessions.size();
        dto.setTotalEnrollments(totalEnrollments);

        // ── Compute average rating ────────────────────────────────────────
        List<Session> ratedSessions = mentorSessions.stream()
                .filter(s -> s.getStudentRating() != null)
                .collect(Collectors.toList());

        double avgRating = ratedSessions.stream()
                .mapToInt(Session::getStudentRating)
                .average()
                .orElse(0.0);

        dto.setAverageRating(Math.round(avgRating * 10.0) / 10.0);
        dto.setReviewCount(ratedSessions.size());

        // ── Build per-subject stats ───────────────────────────────────────
        List<MentorProfileResponseDTO.SubjectStat> subjectStats = mentor.getSubjects()
                .stream()
                .map(subject -> {
                    MentorProfileResponseDTO.SubjectStat stat = new MentorProfileResponseDTO.SubjectStat();
                    stat.setId(subject.getId());
                    stat.setSubjectName(subject.getSubjectName());
                    stat.setDescription(subject.getDescription());
                    stat.setCourseImageUrl(subject.getCourseImageUrl());

                    // Count sessions for this subject
                    long count = mentorSessions.stream()
                            .filter(s -> s.getSubject() != null
                                    && s.getSubject().getId().equals(subject.getId()))
                            .count();
                    stat.setEnrollmentCount((int) count);
                    return stat;
                })
                .collect(Collectors.toList());

        dto.setSubjects(subjectStats);

        // ── Build recent reviews (up to 10 with review text) ─────────────
        List<MentorProfileResponseDTO.ReviewDTO> reviews = mentorSessions.stream()
                .filter(s -> s.getStudentReview() != null && !s.getStudentReview().isBlank())
                .sorted((a, b) -> {
                    if (b.getCreatedAt() == null) return -1;
                    if (a.getCreatedAt() == null) return 1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(10)
                .map(s -> {
                    MentorProfileResponseDTO.ReviewDTO review = new MentorProfileResponseDTO.ReviewDTO();
                    review.setId(s.getId());
                    if (s.getStudent() != null) {
                        review.setStudentName(
                                s.getStudent().getFirstName() + " " + s.getStudent().getLastName());
                    }
                    review.setRating(s.getStudentRating());
                    review.setReviewText(s.getStudentReview());
                    review.setCreatedAt(s.getCreatedAt());
                    return review;
                })
                .collect(Collectors.toList());

        dto.setReviews(reviews);

        return dto;
    }

    // ── Update / Delete ───────────────────────────────────────────────────────

    @CacheEvict(value = "mentors", allEntries = true)
    public Mentor updateMentorById(Long id, Mentor updatedMentor) {
        try {
            Mentor mentor = mentorRepository.findById(id).orElseThrow(
                    () -> new SkillMentorException("Mentor Not found", HttpStatus.NOT_FOUND)
            );
            modelMapper.map(updatedMentor, mentor);
            return mentorRepository.save(mentor);
        } catch (SkillMentorException skillMentorException) {
            log.warn("Mentor not found with id: {} to update", id, skillMentorException);
            throw new SkillMentorException("Mentor Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error updating mentor", exception);
            throw new SkillMentorException("Failed to update mentor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteMentor(Long id) {
        try {
            mentorRepository.deleteById(id);
        } catch (Exception exception) {
            log.error("Failed to delete mentor with id {}", id, exception);
            throw new SkillMentorException("Failed to delete mentor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}