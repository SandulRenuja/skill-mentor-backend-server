package com.stemlink.skillmentor.services.impl;

import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Subject;
import com.stemlink.skillmentor.exceptions.SkillMentorException;
import com.stemlink.skillmentor.respositories.MentorRepository;
import com.stemlink.skillmentor.respositories.SubjectRepository;
import com.stemlink.skillmentor.services.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final MentorRepository mentorRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Subject> getAllSubjects() {
        try {
            return subjectRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to get all subjects", e);
            throw new SkillMentorException("Failed to get all subjects", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Subject addNewSubject(Long mentorId, Subject subject) {
        try {
            Mentor mentor = mentorRepository.findById(mentorId).orElseThrow(
                    () -> new SkillMentorException("Mentor not found", HttpStatus.NOT_FOUND)
            );
            subject.setMentor(mentor);
            return subjectRepository.save(subject);
        } catch (SkillMentorException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while adding subject: {}", e.getMessage());
            throw new SkillMentorException("Subject already exists or database constraint violation", HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Failed to add new subject", e);
            throw new SkillMentorException("Failed to add new subject", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Subject getSubjectById(Long id) {
        return subjectRepository.findById(id).orElseThrow(
                () -> new SkillMentorException("Subject not found", HttpStatus.NOT_FOUND)
        );
    }

    @Override
    public Subject updateSubjectById(Long id, Subject updatedSubject) {
        try {
            Subject subject = subjectRepository.findById(id).orElseThrow(
                    () -> new SkillMentorException("Subject not found", HttpStatus.NOT_FOUND)
            );
            modelMapper.map(updatedSubject, subject);
            return subjectRepository.save(subject);
        } catch (SkillMentorException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating subject: {}", e.getMessage());
            throw new SkillMentorException("Database constraint violation", HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error("Error updating subject", e);
            throw new SkillMentorException("Failed to update subject", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteSubject(Long id) {
        try {
            if (!subjectRepository.existsById(id)) {
                throw new SkillMentorException("Subject not found", HttpStatus.NOT_FOUND);
            }
            subjectRepository.deleteById(id);
        } catch (SkillMentorException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete subject with id {}", id, e);
            throw new SkillMentorException("Failed to delete subject", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}