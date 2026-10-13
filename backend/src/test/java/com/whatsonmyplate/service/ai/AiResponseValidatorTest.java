package com.whatsonmyplate.service.ai;

import com.whatsonmyplate.dto.ai.AiIngredient;
import com.whatsonmyplate.dto.ai.AiInstruction;
import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiResponseValidatorTest {

    private AiResponseValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AiResponseValidator();
    }

    @Test
    void testValidateValidResult() {
        AiRecipeResult result = new AiRecipeResult(
                "Butter Chicken",
                "Rich and creamy chicken curry",
                List.of(new AiIngredient("Chicken", 500.0, "g")),
                List.of(new AiInstruction(1, "Simmer chicken in sauce")),
                15,
                30,
                4,
                SpiceLevel.MEDIUM,
                0.95
        );

        assertDoesNotThrow(() -> validator.validate(result));
    }

    @Test
    void testValidateNullResultThrowsException() {
        assertThrows(AiServiceException.class, () -> validator.validate(null));
    }

    @Test
    void testValidateBlankDishNameThrowsException() {
        AiRecipeResult result = new AiRecipeResult(
                "",
                "Description",
                List.of(new AiIngredient("Chicken", 500.0, "g")),
                List.of(new AiInstruction(1, "Cook")),
                10,
                20,
                2,
                SpiceLevel.MILD,
                0.9
        );

        assertThrows(AiServiceException.class, () -> validator.validate(result));
    }

    @Test
    void testValidateEmptyIngredientsThrowsException() {
        AiRecipeResult result = new AiRecipeResult(
                "Dish",
                "Description",
                new ArrayList<>(),
                List.of(new AiInstruction(1, "Cook")),
                10,
                20,
                2,
                SpiceLevel.MILD,
                0.9
        );

        assertThrows(AiServiceException.class, () -> validator.validate(result));
    }

    @Test
    void testValidateInvalidIngredientNameThrowsException() {
        AiRecipeResult result = new AiRecipeResult(
                "Dish",
                "Description",
                List.of(new AiIngredient("  ", 500.0, "g")),
                List.of(new AiInstruction(1, "Cook")),
                10,
                20,
                2,
                SpiceLevel.MILD,
                0.9
        );

        assertThrows(AiServiceException.class, () -> validator.validate(result));
    }

    @Test
    void testValidateBlankIngredientUnitThrowsException() {
        AiRecipeResult result = new AiRecipeResult(
                "Dish",
                "Description",
                List.of(new AiIngredient("Salt", 1.0, "   ")),
                List.of(new AiInstruction(1, "Cook")),
                10,
                20,
                2,
                SpiceLevel.MILD,
                0.9
        );

        assertThrows(AiServiceException.class, () -> validator.validate(result));
    }

    @Test
    void testValidateNonSequentialInstructionStepsThrowsException() {
        AiRecipeResult result = new AiRecipeResult(
                "Dish",
                "Description",
                List.of(new AiIngredient("Rice", 1.0, "cup")),
                List.of(
                        new AiInstruction(1, "Wash rice"),
                        new AiInstruction(3, "Cook rice") // Step 3 instead of 2
                ),
                10,
                20,
                2,
                SpiceLevel.MILD,
                0.9
        );

        AiServiceException ex = assertThrows(AiServiceException.class, () -> validator.validate(result));
        assertEquals("AI validation failed: Instruction step numbers must be positive and sequential. Expected step 2 but found 3.", ex.getMessage());
    }

    @Test
    void testValidateNegativePrepTimeThrowsException() {
        AiRecipeResult result = new AiRecipeResult(
                "Dish",
                "Description",
                List.of(new AiIngredient("Rice", 1.0, "cup")),
                List.of(new AiInstruction(1, "Boil")),
                -5,
                20,
                2,
                SpiceLevel.MILD,
                0.9
        );

        assertThrows(AiServiceException.class, () -> validator.validate(result));
    }

    @Test
    void testValidateMissingSpiceLevelThrowsException() {
        AiRecipeResult result = new AiRecipeResult(
                "Salad",
                "Fresh salad",
                List.of(new AiIngredient("Lettuce", 1.0, "head")),
                List.of(new AiInstruction(1, "Chop and toss")),
                5,
                0,
                2,
                null, // Missing spice level
                0.85
        );

        assertThrows(AiServiceException.class, () -> validator.validate(result));
    }

    @Test
    void testValidateOutOfRangeConfidenceThrowsException() {
        AiRecipeResult result = new AiRecipeResult(
                "Salad",
                "Fresh salad",
                List.of(new AiIngredient("Lettuce", 1.0, "head")),
                List.of(new AiInstruction(1, "Chop and toss")),
                5,
                0,
                2,
                SpiceLevel.MILD,
                1.5 // Confidence > 1.0
        );

        assertThrows(AiServiceException.class, () -> validator.validate(result));
    }
}
