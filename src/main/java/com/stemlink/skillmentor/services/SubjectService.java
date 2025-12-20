package com.stemlink.skillmentor.services;

import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Subject;
import com.stemlink.skillmentor.respositories.MentorRepository;
import com.stemlink.skillmentor.respositories.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final MentorRepository mentorRepository;
    private final ModelMapper modelMapper;

    public List<Subject> getAllSubjects(){
        return subjectRepository.findAll(); // SELECT * from subject
    }

    public Subject addNewSubject(Long mentorId, Subject subject){
        Mentor mentor = mentorRepository.findById(mentorId).get();
        subject.setMentor(mentor);
        return subjectRepository.save(subject); // INSERT
    }

    public Subject getSubjectById(Long id){
        return subjectRepository.findById(id).get(); // ... WHERE id=={}
    }

    public Subject updateSubjectById(Long id, Subject updatedSubject){
        Subject subject = subjectRepository.findById(id).get();
        subject.setSubjectName(updatedSubject.getSubjectName());
        subject.setDescription(updatedSubject.getDescription());
        return subjectRepository.save(subject);
    }

    public void deleteSubject(Long id){
        subjectRepository.deleteById(id);
    }
}
