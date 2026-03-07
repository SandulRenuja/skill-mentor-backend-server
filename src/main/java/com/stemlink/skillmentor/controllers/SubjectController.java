package com.stemlink.skillmentor.controllers;

import com.stemlink.skillmentor.dto.SubjectDTO;
import com.stemlink.skillmentor.entities.Subject;
import com.stemlink.skillmentor.services.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/subjects")
@RequiredArgsConstructor
public class SubjectController extends AbstractController {

    private final ModelMapper modelMapper;
    private final SubjectService subjectService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return sendOkResponse(subjectService.getAllSubjects());
    }

    @GetMapping("{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Subject> getSubjectById(@PathVariable Long id) {
        return sendOkResponse(subjectService.getSubjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody SubjectDTO subjectDTO) {
        Subject subject = modelMapper.map(subjectDTO, Subject.class);
        return sendCreatedResponse(subjectService.addNewSubject(subjectDTO.getMentorId(), subject));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MENTOR')")
    public ResponseEntity<Subject> updateSubject(
            @PathVariable Long id,
            @RequestBody SubjectDTO updatedSubjectDTO) {
        Subject subject = modelMapper.map(updatedSubjectDTO, Subject.class);
        return sendOkResponse(subjectService.updateSubjectById(id, subject));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return sendNoContentResponse();
    }
}