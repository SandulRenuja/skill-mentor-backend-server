package com.stemlink.skillmentor.controllers;

import com.stemlink.skillmentor.dto.SessionDTO;
import com.stemlink.skillmentor.dto.response.AdminSessionResponseDTO;
import com.stemlink.skillmentor.dto.response.SessionResponseDTO;
import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.security.UserPrincipal;
import com.stemlink.skillmentor.services.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/sessions")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class SessionController extends AbstractController {

    private final SessionService sessionService;

    // ─── Admin: all sessions ──────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminSessionResponseDTO>> getAllSessions() {
        List<Session> sessions = sessionService.getAllSessions();
        List<AdminSessionResponseDTO> response = sessions.stream()
                .map(this::toAdminSessionResponseDTO)
                .collect(Collectors.toList());
        return sendOkResponse(response);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Session> getSessionById(@PathVariable Long id) {
        return sendOkResponse(sessionService.getSessionById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Session> createSession(@Valid @RequestBody SessionDTO sessionDTO) {
        return sendCreatedResponse(sessionService.createNewSession(sessionDTO));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Session> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody SessionDTO updatedSessionDTO) {
        return sendOkResponse(sessionService.updateSessionById(id, updatedSessionDTO));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return sendNoContentResponse();
    }

    // ─── Student: enroll ─────────────────────────────────────────────────────

    @PostMapping("/enroll")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<SessionResponseDTO> enroll(
            @RequestBody SessionDTO sessionDTO,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Session session = sessionService.enrollSession(userPrincipal, sessionDTO);
        return sendCreatedResponse(toSessionResponseDTO(session));
    }

    @GetMapping("/my-sessions")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<List<SessionResponseDTO>> getMySessions(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<Session> sessions = sessionService.getSessionsByStudentEmail(userPrincipal.getEmail());
        List<SessionResponseDTO> response = sessions.stream()
                .map(this::toSessionResponseDTO)
                .collect(Collectors.toList());
        return sendOkResponse(response);
    }

    // ─── Mappers ─────────────────────────────────────────────────────────────

    private SessionResponseDTO toSessionResponseDTO(Session session) {
        SessionResponseDTO dto = new SessionResponseDTO();
        dto.setId(session.getId());
        dto.setMentorName(session.getMentor().getFirstName() + " " + session.getMentor().getLastName());
        dto.setMentorProfileImageUrl(session.getMentor().getProfileImageUrl());
        dto.setSubjectName(session.getSubject().getSubjectName());
        dto.setSessionAt(session.getSessionAt());
        dto.setDurationMinutes(session.getDurationMinutes());
        dto.setSessionStatus(session.getSessionStatus());
        dto.setPaymentStatus(session.getPaymentStatus());
        dto.setMeetingLink(session.getMeetingLink());
        return dto;
    }

    private AdminSessionResponseDTO toAdminSessionResponseDTO(Session session) {
        AdminSessionResponseDTO dto = new AdminSessionResponseDTO();
        dto.setId(session.getId());

        // Student info
        if (session.getStudent() != null) {
            dto.setStudentName(session.getStudent().getFirstName()
                    + " " + session.getStudent().getLastName());
            dto.setStudentEmail(session.getStudent().getEmail());
        }

        // Mentor info
        if (session.getMentor() != null) {
            dto.setMentorName(session.getMentor().getFirstName()
                    + " " + session.getMentor().getLastName());
        }

        // Subject info
        if (session.getSubject() != null) {
            dto.setSubjectName(session.getSubject().getSubjectName());
        }

        dto.setSessionAt(session.getSessionAt());
        dto.setDurationMinutes(session.getDurationMinutes());
        dto.setSessionStatus(session.getSessionStatus());
        dto.setPaymentStatus(session.getPaymentStatus());
        dto.setMeetingLink(session.getMeetingLink());
        return dto;
    }
}