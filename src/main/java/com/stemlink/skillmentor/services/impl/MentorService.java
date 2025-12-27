package com.stemlink.skillmentor.services.impl;

import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.exceptions.SkillMentorException;
import com.stemlink.skillmentor.respositories.MentorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentorService {

    private final MentorRepository mentorRepository;
    private final ModelMapper modelMapper;

    public Mentor createNewMentor(Mentor mentor) {
        try {
            return mentorRepository.save(mentor);
        } catch (Exception exception) {
            // What, When, Where, Why
            //System.err.println("Error creating mentor" + exception.getMessage());
            throw new SkillMentorException("Failed to create new mentor", HttpStatus.CONFLICT);
        }
    }

    public List<Mentor> getAllMentors() {
        log.debug("getting mentors");
        return mentorRepository.findAll(); // SELECT * FROM mentor
    }

    public Mentor getMentorById(Long id) {
        try {

            Mentor mentor = mentorRepository.findById(id).orElseThrow(
                    () -> new SkillMentorException("Mentor Not found", HttpStatus.NOT_FOUND)
            );
            log.info("Successfully fetched mentor {}",id);
            return mentor;
        } catch (SkillMentorException skillMentorException) {
            //System.err.println("Mentor not found " + skillMentorException.getMessage());
            // LOG LEVELS
            // DEBUG, INFO, WARN, ERROR
            // env - dev, prod
            log.warn("Mentor not found", skillMentorException);
            throw new SkillMentorException("Mentor Not found", HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            log.error("Error getting mentor", exception);
            throw new SkillMentorException("Failed to get mentor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Mentor updateMentorById(Long id, Mentor updatedMentor) {
        Mentor mentor = mentorRepository.findById(id).get();
        modelMapper.map(updatedMentor, mentor);
        return mentorRepository.save(mentor);
    }

    public void deleteMentor(Long id) {
        mentorRepository.deleteById(id);
    }

}
