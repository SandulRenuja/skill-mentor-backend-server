package com.stemlink.skillmentor.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Date;

/**
 * DTO for session creation and updates.
 * studentId, mentorId, subjectId are optional on PUT (admin partial updates).
 * studentId is derived from JWT on the /enroll endpoint so it is not validated there.
 */
@Data
public class SessionDTO {

    // Optional on PUT (admin updating payment/session status only)
    private Integer studentId;

    private Long mentorId;

    private Long subjectId;

    private Date sessionAt;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    private String sessionStatus;

    private String meetingLink;

    private String sessionNotes;

    private String studentReview;

    @Min(value = 1, message = "Rating must be at least 1")
    private Integer studentRating;

    private String paymentStatus;
}