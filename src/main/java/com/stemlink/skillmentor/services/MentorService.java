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
public class MentorService {

    private final MentorRepository mentorRepository;
    private final ModelMapper modelMapper;

    public Mentor createNewMentor(Mentor mentor){
        return mentorRepository.save(mentor);
    }

    public List<Mentor> getAllMentors(){
        return mentorRepository.findAll(); // SELECT * FROM mentor
    }

    public Mentor getMentorById(Long id){
        return mentorRepository.findById(id).get();
    }
    public Mentor updateMentorById(Long id, Mentor updatedMentor){
        Mentor mentor = mentorRepository.findById(id).get();
        modelMapper.map(updatedMentor, mentor);
        return mentorRepository.save(mentor);
    }

    public void deleteMentor(Long id){
        mentorRepository.deleteById(id);
    }

}
