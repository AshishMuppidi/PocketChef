package com.whatsonmyplate.repository;

import com.whatsonmyplate.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByRecipeId(Long recipeId);

    List<Rating> findByCommunityPostId(Long communityPostId);

    Optional<Rating> findByUserIdAndRecipeId(Long userId, Long recipeId);

    Optional<Rating> findByUserIdAndCommunityPostId(Long userId, Long communityPostId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.recipe.id = :recipeId")
    Double findAverageScoreByRecipeId(@Param("recipeId") Long recipeId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.communityPost.id = :postId")
    Double findAverageScoreByCommunityPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.communityPost.id = :postId")
    Long countByCommunityPostId(@Param("postId") Long postId);
}
