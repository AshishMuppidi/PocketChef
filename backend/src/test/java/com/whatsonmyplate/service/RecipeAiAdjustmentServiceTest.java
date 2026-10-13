package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.ai.AdjustedIngredient;
import com.whatsonmyplate.dto.ai.IngredientSubstitutionRequest;
import com.whatsonmyplate.dto.ai.IngredientSubstitutionResult;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentRequest;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentResult;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.entity.model.IngredientItem;
import com.whatsonmyplate.exception.ResourceNotFoundException;
import com.whatsonmyplate.repository.RecipeRepository;
import com.whatsonmyplate.service.ai.AiRecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeAiAdjustmentServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private AiRecipeService aiRecipeService;

    private RecipeAiAdjustmentService adjustmentService;

    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        adjustmentService = new RecipeAiAdjustmentService(recipeRepository, aiRecipeService);

        testRecipe = new Recipe();
        testRecipe.setId(10L);
        testRecipe.setTitle("Spicy Chicken Pasta");
        testRecipe.setSpiceLevel(SpiceLevel.SPICY);
        testRecipe.setIngredients(List.of(
                new IngredientItem("Chicken Breast", 500.0, "g"),
                new IngredientItem("Heavy Cream", 1.0, "cup"),
                new IngredientItem("Red Chili Flakes", 2.0, "tsp")
        ));
    }

    @Test
    void testGetIngredientSubstitutionSuccess() {
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(testRecipe));

        IngredientSubstitutionResult expected = new IngredientSubstitutionResult(
                "Heavy Cream", "Milk + Butter", "3/4 cup milk + 1/4 cup butter",
                "Similar fat content", "Whisk together melted butter and warm milk", 0.92
        );

        when(aiRecipeService.generateIngredientSubstitution(eq(testRecipe), eq("Heavy Cream"), eq("No dairy")))
                .thenReturn(expected);

        IngredientSubstitutionRequest request = new IngredientSubstitutionRequest("Heavy Cream", "No dairy");
        IngredientSubstitutionResult result = adjustmentService.getIngredientSubstitution(10L, request);

        assertNotNull(result);
        assertEquals("Milk + Butter", result.getSubstitute());
        assertEquals(0.92, result.getConfidence());
    }

    @Test
    void testGetIngredientSubstitutionIngredientNotFoundInRecipeThrows400() {
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(testRecipe));

        IngredientSubstitutionRequest request = new IngredientSubstitutionRequest("Avocado", "Not available");

        assertThrows(IllegalArgumentException.class, () -> adjustmentService.getIngredientSubstitution(10L, request));
    }

    @Test
    void testGetIngredientSubstitutionRecipeNotFoundThrows404() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        IngredientSubstitutionRequest request = new IngredientSubstitutionRequest("Heavy Cream", null);

        assertThrows(ResourceNotFoundException.class, () -> adjustmentService.getIngredientSubstitution(99L, request));
    }

    @Test
    void testGetSpiceAdjustmentSuccess() {
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(testRecipe));

        SpiceAdjustmentResult expected = new SpiceAdjustmentResult(
                10L, "SPICY", "MILD",
                List.of(new AdjustedIngredient("Red Chili Flakes", 2.0, "tsp", 0.5, "tsp", "Reduced heat")),
                "Mild version preserves flavor", 0.95
        );

        when(aiRecipeService.generateSpiceAdjustment(eq(testRecipe), eq(SpiceLevel.MILD)))
                .thenReturn(expected);

        SpiceAdjustmentRequest request = new SpiceAdjustmentRequest("MILD");
        SpiceAdjustmentResult result = adjustmentService.getSpiceAdjustment(10L, request);

        assertNotNull(result);
        assertEquals("MILD", result.getTargetSpiceLevel());
        assertEquals(1, result.getAdjustedIngredients().size());
        assertEquals(0.5, result.getAdjustedIngredients().get(0).getAdjustedQuantity());
    }

    @Test
    void testGetSpiceAdjustmentInvalidLevelThrows400() {
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(testRecipe));

        SpiceAdjustmentRequest request = new SpiceAdjustmentRequest("EXTREME_HOT");

        assertThrows(IllegalArgumentException.class, () -> adjustmentService.getSpiceAdjustment(10L, request));
    }
}
