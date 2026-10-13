package com.whatsonmyplate.repository;

import com.whatsonmyplate.entity.SavedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedRecipeRepository extends JpaRepository<SavedRecipe, Long> {

    List<SavedRecipe> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<SavedRecipe> findByUserIdAndRecipeId(Long userId, Long recipeId);

    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);

    void deleteByUserIdAndRecipeId(Long userId, Long recipeId);
}
