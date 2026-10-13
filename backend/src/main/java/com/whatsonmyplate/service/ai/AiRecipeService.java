package com.whatsonmyplate.service.ai;

import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.dto.ai.IngredientSubstitutionResult;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentResult;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.SpiceLevel;

public interface AiRecipeService {

    /**
     * Internal/testable foundation method to generate a structured recipe from a text prompt.
     *
     * @param prompt The descriptive dish prompt or recipe inquiry.
     * @return Validated structured AiRecipeResult.
     */
    AiRecipeResult generateRecipeFromText(String prompt);

    /**
     * Multimodal analysis foundation method to generate a structured recipe from image data.
     *
     * @param imageBytes The raw binary image data.
     * @param mimeType The image MIME type (e.g., "image/jpeg", "image/png").
     * @param additionalPrompt Optional additional culinary instructions or context.
     * @return Validated structured AiRecipeResult.
     */
    AiRecipeResult generateRecipeFromImage(byte[] imageBytes, String mimeType, String additionalPrompt);

    /**
     * Generates a smart ingredient substitution recommendation for a recipe.
     *
     * @param recipe The recipe context.
     * @param ingredientName The target ingredient to replace.
     * @param reason Optional substitution constraint (e.g., dietary, pantry shortage).
     * @return Validated structured IngredientSubstitutionResult.
     */
    IngredientSubstitutionResult generateIngredientSubstitution(Recipe recipe, String ingredientName, String reason);

    /**
     * Generates an AI-adjusted spice profile for a recipe.
     *
     * @param recipe The recipe context.
     * @param targetSpiceLevel The desired spice level (MILD, MEDIUM, SPICY).
     * @return Validated structured SpiceAdjustmentResult.
     */
    SpiceAdjustmentResult generateSpiceAdjustment(Recipe recipe, SpiceLevel targetSpiceLevel);
}
