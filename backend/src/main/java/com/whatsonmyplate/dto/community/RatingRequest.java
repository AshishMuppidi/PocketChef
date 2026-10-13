package com.whatsonmyplate.dto.community;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RatingRequest {

    @NotNull(message = "Rating score is required")
    @Min(value = 1, message = "Rating score must be at least 1")
    @Max(value = 5, message = "Rating score must be at most 5")
    private Integer score;

    @Size(max = 1000, message = "Review must not exceed 1000 characters")
    private String review;

    public RatingRequest() {
    }

    public RatingRequest(Integer score, String review) {
        this.score = score;
        this.review = review;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
