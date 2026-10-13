package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.dto.recipe.IngredientItemDto;
import com.whatsonmyplate.dto.recipe.InstructionStepDto;
import com.whatsonmyplate.dto.recipe.RecipeResponse;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.entity.model.IngredientItem;
import com.whatsonmyplate.entity.model.InstructionStep;
import com.whatsonmyplate.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(RecipePersistenceService.class);

    private final RecipeRepository recipeRepository;

    public RecipePersistenceService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /**
     * Persists an AI-generated recipe inside a dedicated database transaction.
     * Invoked via Spring proxy from ImageRecipeService to guarantee transaction boundaries.
     *
     * @param aiResult Validated AI recipe result.
     * @param imageUrl Permanent secure Cloudinary image URL (optional).
     * @return Saved RecipeResponse.
     */
    @Transactional
    public RecipeResponse saveAiRecipe(AiRecipeResult aiResult, String imageUrl) {
        Recipe recipe = new Recipe();
        recipe.setTitle(aiResult.getDishName());
        recipe.setDescription(aiResult.getDescription());
        recipe.setImageUrl(imageUrl);
        recipe.setSource(RecipeSource.AI_GENERATED);
        recipe.setSourceId(null);
        recipe.setIsAiGenerated(true);
        recipe.setPrepTimeMinutes(aiResult.getPrepTimeMinutes());
        recipe.setCookTimeMinutes(aiResult.getCookTimeMinutes());
        recipe.setServings(aiResult.getServings() != null ? aiResult.getServings() : 1);
        recipe.setSpiceLevel(aiResult.getSpiceLevel() != null ? aiResult.getSpiceLevel() : SpiceLevel.MEDIUM);

        if (aiResult.getIngredients() != null) {
            List<IngredientItem> ingredients = aiResult.getIngredients().stream()
                    .map(dto -> new IngredientItem(dto.getName(), dto.getQuantity(), dto.getUnit()))
                    .collect(Collectors.toList());
            recipe.setIngredients(ingredients);
        } else {
            recipe.setIngredients(new ArrayList<>());
        }

        if (aiResult.getInstructions() != null) {
            List<InstructionStep> instructions = aiResult.getInstructions().stream()
                    .map(dto -> new InstructionStep(dto.getStep(), dto.getText()))
                    .collect(Collectors.toList());
            recipe.setInstructions(instructions);
        } else {
            recipe.setInstructions(new ArrayList<>());
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        logger.info("Successfully persisted AI recipe [ID: {}, Title: '{}', ImageUrl: '{}'] in database transaction",
                savedRecipe.getId(), savedRecipe.getTitle(), savedRecipe.getImageUrl());

        return mapToResponse(savedRecipe);
    }

    private RecipeResponse mapToResponse(Recipe recipe) {
        List<IngredientItemDto> ingredientDtos = recipe.getIngredients() != null
                ? recipe.getIngredients().stream()
                .map(item -> new IngredientItemDto(item.getName(), item.getQuantity(), item.getUnit()))
                .collect(Collectors.toList())
                : List.of();

        List<InstructionStepDto> instructionDtos = recipe.getInstructions() != null
                ? recipe.getInstructions().stream()
                .map(step -> new InstructionStepDto(step.getStep(), step.getText()))
                .collect(Collectors.toList())
                : List.of();

        return new RecipeResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getImageUrl(),
                recipe.getSource(),
                recipe.getSourceId(),
                recipe.getIsAiGenerated(),
                recipe.getPrepTimeMinutes(),
                recipe.getCookTimeMinutes(),
                recipe.getServings(),
                recipe.getSpiceLevel(),
                ingredientDtos,
                instructionDtos,
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }
}
