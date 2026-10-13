package com.whatsonmyplate.dto.auth;

import java.time.LocalDateTime;

public class UserProfileDto {

    private Long id;
    private String email;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private LocalDateTime createdAt;

    public UserProfileDto() {
    }

    public UserProfileDto(Long id, String email, String username, String displayName, String profileImageUrl, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
