package com.whatsonmyplate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.Check;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
    name = "ratings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ratings_user_recipe", columnNames = {"user_id", "recipe_id"}),
        @UniqueConstraint(name = "uk_ratings_user_post", columnNames = {"user_id", "community_post_id"})
    },
    indexes = {
        @Index(name = "idx_ratings_user", columnList = "user_id"),
        @Index(name = "idx_ratings_recipe", columnList = "recipe_id"),
        @Index(name = "idx_ratings_post", columnList = "community_post_id")
    }
)
@Check(constraints = "(recipe_id IS NOT NULL AND community_post_id IS NULL) OR (recipe_id IS NULL AND community_post_id IS NOT NULL)")
public class Rating implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_post_id")
    private CommunityPost communityPost;

    @Min(1)
    @Max(5)
    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Rating() {
    }

    public Rating(User user, Recipe recipe, Integer score, String review) {
        this.user = user;
        this.recipe = recipe;
        this.communityPost = null;
        this.score = score;
        this.review = review;
    }

    public Rating(User user, CommunityPost communityPost, Integer score, String review) {
        this.user = user;
        this.recipe = null;
        this.communityPost = communityPost;
        this.score = score;
        this.review = review;
    }

    /**
     * Enforces the rule that a rating must target exactly one entity:
     * either a Recipe OR a CommunityPost (never both, never neither).
     */
    @AssertTrue(message = "Rating must be associated with either a recipe or a community post, but not both.")
    public boolean isTargetValid() {
        return (recipe != null && communityPost == null) || (recipe == null && communityPost != null);
    }

    @PrePersist
    protected void onCreate() {
        if (!isTargetValid()) {
            throw new IllegalStateException("Rating must target either a recipe or a community post, never both or neither.");
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        if (!isTargetValid()) {
            throw new IllegalStateException("Rating must target either a recipe or a community post, never both or neither.");
        }
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public CommunityPost getCommunityPost() {
        return communityPost;
    }

    public void setCommunityPost(CommunityPost communityPost) {
        this.communityPost = communityPost;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating rating)) return false;
        return Objects.equals(id, rating.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", score=" + score +
                ", review='" + review + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
