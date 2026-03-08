package com.stemlink.skillmentor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for submitting a student review on a completed session.
 * Maps to PATCH /api/v1/sessions/{id}/review
 */
@Data
public class ReviewDTO {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @NotNull(message = "Review text is required")
    @Size(min = 10, max = 1000, message = "Review must be between 10 and 1000 characters")
    private String reviewText;
}