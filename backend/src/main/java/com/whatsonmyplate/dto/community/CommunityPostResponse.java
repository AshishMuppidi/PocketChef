package com.whatsonmyplate.dto.community;

import java.time.LocalDateTime;

public class CommunityPostResponse {

    private Long id;
    private Long userId;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private Long recipeId;
    private String recipeTitle;
    private String imageUrl;
    private String caption;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likeCount;
    private Long commentCount;
    private Double averageRating;
    private Long ratingCount;
    private Boolean isLikedByCurrentUser;
    private Integer currentUserRating;

    public CommunityPostResponse() {
    }

    public CommunityPostResponse(
            Long id,
            Long userId,
            String username,
            String displayName,
            String profileImageUrl,
            Long recipeId,
            String recipeTitle,
            String imageUrl,
            String caption,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long likeCount,
            Long commentCount,
            Double averageRating,
            Long ratingCount,
            Boolean isLikedByCurrentUser,
            Integer currentUserRating
    ) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.recipeId = recipeId;
        this.recipeTitle = recipeTitle;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likeCount = likeCount != null ? likeCount : 0L;
        this.commentCount = commentCount != null ? commentCount : 0L;
        this.averageRating = averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0;
        this.ratingCount = ratingCount != null ? ratingCount : 0L;
        this.isLikedByCurrentUser = isLikedByCurrentUser != null ? isLikedByCurrentUser : false;
        this.currentUserRating = currentUserRating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeTitle() {
        return recipeTitle;
    }

    public void setRecipeTitle(String recipeTitle) {
        this.recipeTitle = recipeTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
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

    public Boolean getIsLikedByCurrentUser() {
        return isLikedByCurrentUser;
    }

    public void setIsLikedByCurrentUser(Boolean isLikedByCurrentUser) {
        this.isLikedByCurrentUser = isLikedByCurrentUser;
    }

    public Integer getCurrentUserRating() {
        return currentUserRating;
    }

    public void setCurrentUserRating(Integer currentUserRating) {
        this.currentUserRating = currentUserRating;
    }
}
