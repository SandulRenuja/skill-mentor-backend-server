package com.stemlink.skillmentor.dto.response;

import lombok.Data;

import java.util.Date;

/**
 * Session response DTO for admin use.
 * Includes student identity fields not exposed in the student-facing SessionResponseDTO.
 */
@Data
public class AdminSessionResponseDTO {
    private Integer id;

    // Student
    private String studentName;
    private String studentEmail;

    // Mentor
    private String mentorName;

    // Subject
    private String subjectName;

    // Session details
    private Date sessionAt;
    private Integer durationMinutes;
    private String sessionStatus;
    private String paymentStatus;
    private String meetingLink;
}