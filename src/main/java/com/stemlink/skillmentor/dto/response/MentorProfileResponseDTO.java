package com.stemlink.skillmentor.dto.response;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Rich mentor profile DTO returned by GET /api/v1/mentors/{id}/profile.
 * Includes computed stats (avgRating, totalEnrollments) and subject details
 * with per-subject enrollment counts.
 */
@Data
public class MentorProfileResponseDTO {

    // Identity
    private Long id;
    private String mentorId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String title;
    private String profession;
    private String company;
    private int experienceYears;
    private String bio;
    private String profileImageUrl;
    private Boolean isCertified;
    private String startYear;

    // Computed stats
    private Integer positiveReviews;       // stored percentage
    private Integer totalEnrollments;      // sum across all subjects
    private Double averageRating;          // calculated from reviews
    private Integer reviewCount;

    // Subjects with enrollment counts
    private List<SubjectStat> subjects;

    // Recent reviews
    private List<ReviewDTO> reviews;

    @Data
    public static class SubjectStat {
        private Long id;
        private String subjectName;
        private String description;
        private String courseImageUrl;
        private Integer enrollmentCount;
    }

    @Data
    public static class ReviewDTO {
        private Integer id;
        private String studentName;
        private Integer rating;
        private String reviewText;
        private Date createdAt;
    }
}