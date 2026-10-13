package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.ai.AiIngredient;
import com.whatsonmyplate.dto.ai.AiInstruction;
import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.dto.recipe.RecipeResponse;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipePersistenceServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    private RecipePersistenceService persistenceService;

    @BeforeEach
    void setUp() {
        persistenceService = new RecipePersistenceService(recipeRepository);
    }

    @Test
    void testSaveAiRecipeSetsExpectedFieldsWithImageUrl() {
        AiRecipeResult aiResult = new AiRecipeResult(
                "Butter Chicken",
                "Rich and creamy curry",
                List.of(new AiIngredient("Chicken", 500.0, "g")),
                List.of(new AiInstruction(1, "Cook chicken")),
                15,
                30,
                4,
                SpiceLevel.MEDIUM,
                0.95
        );

        String sampleCloudinaryUrl = "https://res.cloudinary.com/demo/image/upload/v12345/whatsonmyplate/recipes/butter-chicken.jpg";

        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(50L);
            return r;
        });

        RecipeResponse response = persistenceService.saveAiRecipe(aiResult, sampleCloudinaryUrl);

        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals("Butter Chicken", response.getTitle());
        assertEquals(RecipeSource.AI_GENERATED, response.getSource());
        assertTrue(response.getIsAiGenerated());
        assertNull(response.getSourceId());
        assertEquals(sampleCloudinaryUrl, response.getImageUrl());
        assertEquals(1, response.getIngredients().size());
        assertEquals(1, response.getInstructions().size());

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(captor.capture());
        Recipe saved = captor.getValue();
        assertEquals("Butter Chicken", saved.getTitle());
        assertEquals(RecipeSource.AI_GENERATED, saved.getSource());
        assertTrue(saved.getIsAiGenerated());
        assertNull(saved.getSourceId());
        assertEquals(sampleCloudinaryUrl, saved.getImageUrl());
    }
}
