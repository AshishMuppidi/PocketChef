package com.whatsonmyplate.repository;

import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long>, JpaSpecificationExecutor<Recipe> {

    List<Recipe> findByTitleContainingIgnoreCase(String keyword);

    List<Recipe> findBySource(RecipeSource source);

    List<Recipe> findBySpiceLevel(SpiceLevel spiceLevel);

    Optional<Recipe> findBySourceAndSourceId(RecipeSource source, String sourceId);
}
