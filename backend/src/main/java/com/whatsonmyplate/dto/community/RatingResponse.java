package com.whatsonmyplate.dto.community;

public class RatingResponse {

    private Double averageRating;
    private Long ratingCount;
    private Integer currentUserRating;

    public RatingResponse() {
    }

    public RatingResponse(Double averageRating, Long ratingCount, Integer currentUserRating) {
        this.averageRating = averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0;
        this.ratingCount = ratingCount != null ? ratingCount : 0L;
        this.currentUserRating = currentUserRating;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public Integer getCurrentUserRating() {
        return currentUserRating;
    }

    public void setCurrentUserRating(Integer currentUserRating) {
        this.currentUserRating = currentUserRating;
    }
}
