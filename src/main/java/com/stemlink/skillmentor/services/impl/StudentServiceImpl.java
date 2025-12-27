package com.stemlink.skillmentor.services.impl;

import com.stemlink.skillmentor.entities.Student;
import com.stemlink.skillmentor.respositories.StudentRepository;
import com.stemlink.skillmentor.services.StudentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    //TODO: handle exceptions

    public Student createNewStudent(Student student) {
        return studentRepository.save(student);
    }

    // TODO: add pagination
    public List<Student> getAllStudents() {
        return studentRepository.findAll(); // SELECT * FROM student
    }

    public Student getStudentById(Integer id) {
        return studentRepository.findById(id).get();
    }

    public Student updateStudentById(Integer id, Student updatedStudent) {
        Student student = studentRepository.findById(id).get();
        modelMapper.map(updatedStudent, student);
        return studentRepository.save(student);
    }

    public void deleteStudent(Integer id) {
        studentRepository.deleteById(id);
    }

}
