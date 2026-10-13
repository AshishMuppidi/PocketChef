package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.ai.IngredientSubstitutionRequest;
import com.whatsonmyplate.dto.ai.IngredientSubstitutionResult;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentRequest;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentResult;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.exception.ResourceNotFoundException;
import com.whatsonmyplate.repository.RecipeRepository;
import com.whatsonmyplate.service.ai.AiRecipeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RecipeAiAdjustmentService {

    private final RecipeRepository recipeRepository;
    private final AiRecipeService aiRecipeService;

    public RecipeAiAdjustmentService(RecipeRepository recipeRepository, AiRecipeService aiRecipeService) {
        this.recipeRepository = recipeRepository;
        this.aiRecipeService = aiRecipeService;
    }

    @Transactional(readOnly = true)
    public IngredientSubstitutionResult getIngredientSubstitution(Long recipeId, IngredientSubstitutionRequest request) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + recipeId));

        String requestedName = request.getIngredientName().trim().toLowerCase(Locale.ROOT);
        boolean ingredientExists = recipe.getIngredients() != null && recipe.getIngredients().stream().anyMatch(i -> {
            String item = i.getName().toLowerCase(Locale.ROOT).trim();
            return item.contains(requestedName) || requestedName.contains(item);
        });

        if (!ingredientExists) {
            throw new IllegalArgumentException(
                    "Ingredient '" + request.getIngredientName() + "' does not exist in recipe '" + recipe.getTitle() + "'."
            );
        }

        return aiRecipeService.generateIngredientSubstitution(
                recipe,
                request.getIngredientName().trim(),
                request.getReason()
        );
    }

    @Transactional(readOnly = true)
    public SpiceAdjustmentResult getSpiceAdjustment(Long recipeId, SpiceAdjustmentRequest request) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + recipeId));

        SpiceLevel targetLevel;
        try {
            targetLevel = SpiceLevel.valueOf(request.getTargetSpiceLevel().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid spice level: '" + request.getTargetSpiceLevel() + "'. Allowed values are MILD, MEDIUM, SPICY.");
        }

        return aiRecipeService.generateSpiceAdjustment(recipe, targetLevel);
    }
}
