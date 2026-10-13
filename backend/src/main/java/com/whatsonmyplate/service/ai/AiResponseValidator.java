package com.whatsonmyplate.service.ai;

import com.whatsonmyplate.dto.ai.AdjustedIngredient;
import com.whatsonmyplate.dto.ai.AiIngredient;
import com.whatsonmyplate.dto.ai.AiInstruction;
import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.dto.ai.IngredientSubstitutionResult;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentResult;
import com.whatsonmyplate.entity.model.IngredientItem;
import com.whatsonmyplate.exception.AiServiceException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class AiResponseValidator {

    public void validate(AiRecipeResult result) {
        if (result == null) {
            throw new AiServiceException("AI service returned a null recipe response.");
        }

        if (result.getDishName() == null || result.getDishName().trim().isEmpty()) {
            throw new AiServiceException("AI validation failed: Dish name must not be blank.");
        }

        List<AiIngredient> ingredients = result.getIngredients();
        if (ingredients == null || ingredients.isEmpty()) {
            throw new AiServiceException("AI validation failed: Ingredients list must not be empty.");
        }

        for (int i = 0; i < ingredients.size(); i++) {
            AiIngredient ingredient = ingredients.get(i);
            if (ingredient == null) {
                throw new AiServiceException("AI validation failed: Ingredient at index " + i + " is null.");
            }
            if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
                throw new AiServiceException("AI validation failed: Ingredient name at index " + i + " must not be blank.");
            }
            if (ingredient.getQuantity() != null && ingredient.getQuantity() <= 0) {
                throw new AiServiceException("AI validation failed: Quantity for ingredient '" + ingredient.getName() + "' must be greater than zero.");
            }
            if (ingredient.getUnit() == null || ingredient.getUnit().trim().isEmpty()) {
                throw new AiServiceException("AI validation failed: Unit for ingredient '" + ingredient.getName() + "' must not be blank.");
            }
        }

        List<AiInstruction> instructions = result.getInstructions();
        if (instructions == null || instructions.isEmpty()) {
            throw new AiServiceException("AI validation failed: Instruction steps must not be empty.");
        }

        for (int i = 0; i < instructions.size(); i++) {
            AiInstruction instruction = instructions.get(i);
            if (instruction == null) {
                throw new AiServiceException("AI validation failed: Instruction step at index " + i + " is null.");
            }
            int expectedStepNumber = i + 1;
            if (instruction.getStep() == null || instruction.getStep() != expectedStepNumber) {
                throw new AiServiceException("AI validation failed: Instruction step numbers must be positive and sequential. Expected step " + expectedStepNumber + " but found " + instruction.getStep() + ".");
            }
            if (instruction.getText() == null || instruction.getText().trim().isEmpty()) {
                throw new AiServiceException("AI validation failed: Instruction step " + expectedStepNumber + " text must not be blank.");
            }
        }

        if (result.getPrepTimeMinutes() != null && result.getPrepTimeMinutes() < 0) {
            throw new AiServiceException("AI validation failed: Preparation time cannot be negative.");
        }

        if (result.getCookTimeMinutes() != null && result.getCookTimeMinutes() < 0) {
            throw new AiServiceException("AI validation failed: Cooking time cannot be negative.");
        }

        if (result.getServings() != null && result.getServings() <= 0) {
            throw new AiServiceException("AI validation failed: Servings must be greater than zero.");
        }

        if (result.getSpiceLevel() == null) {
            throw new AiServiceException("AI validation failed: Spice level must be specified (MILD, MEDIUM, or SPICY).");
        }

        if (result.getConfidence() == null || result.getConfidence() < 0.0 || result.getConfidence() > 1.0) {
            throw new AiServiceException("AI validation failed: Confidence must be between 0.0 and 1.0.");
        }
    }

    public void validateSubstitution(IngredientSubstitutionResult result, String requestedIngredient) {
        if (result == null) {
            throw new AiServiceException("AI service returned a null substitution response.");
        }

        if (result.getOriginalIngredient() == null || result.getOriginalIngredient().trim().isEmpty()) {
            result.setOriginalIngredient(requestedIngredient);
        }

        if (result.getSubstitute() == null || result.getSubstitute().trim().isEmpty()) {
            throw new AiServiceException("AI validation failed: Substitute must not be blank.");
        }

        if (result.getReason() == null || result.getReason().trim().isEmpty()) {
            throw new AiServiceException("AI validation failed: Substitution reason must not be blank.");
        }

        if (result.getConfidence() == null || result.getConfidence() < 0.0 || result.getConfidence() > 1.0) {
            throw new AiServiceException("AI validation failed: Confidence must be between 0.0 and 1.0.");
        }
    }

    public void validateSpiceAdjustment(SpiceAdjustmentResult result, List<IngredientItem> originalIngredients) {
        if (result == null) {
            throw new AiServiceException("AI service returned a null spice adjustment response.");
        }

        if (result.getTargetSpiceLevel() == null || result.getTargetSpiceLevel().trim().isEmpty()) {
            throw new AiServiceException("AI validation failed: Target spice level must not be blank.");
        }

        if (result.getConfidence() == null || result.getConfidence() < 0.0 || result.getConfidence() > 1.0) {
            throw new AiServiceException("AI validation failed: Confidence must be between 0.0 and 1.0.");
        }

        if (result.getAdjustedIngredients() != null && originalIngredients != null) {
            for (AdjustedIngredient adj : result.getAdjustedIngredients()) {
                if (adj.getName() == null || adj.getName().trim().isEmpty()) {
                    throw new AiServiceException("AI validation failed: Adjusted ingredient name must not be blank.");
                }

                String normAdjName = adj.getName().toLowerCase(Locale.ROOT).trim();
                boolean matchesRecipeIngredient = originalIngredients.stream().anyMatch(orig -> {
                    String normOrig = orig.getName().toLowerCase(Locale.ROOT).trim();
                    return normOrig.contains(normAdjName) || normAdjName.contains(normOrig);
                });

                if (!matchesRecipeIngredient) {
                    throw new AiServiceException("AI validation failed: Adjusted ingredient '" + adj.getName() + "' is not part of the original recipe.");
                }
            }
        }
    }
}
