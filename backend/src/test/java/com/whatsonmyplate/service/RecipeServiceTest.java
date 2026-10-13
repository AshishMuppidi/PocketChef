package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.ai.AiIngredient;
import com.whatsonmyplate.dto.ai.AiInstruction;
import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.dto.recipe.IngredientItemDto;
import com.whatsonmyplate.dto.recipe.InstructionStepDto;
import com.whatsonmyplate.dto.recipe.RecipeCreateRequest;
import com.whatsonmyplate.dto.recipe.RecipePageResponse;
import com.whatsonmyplate.dto.recipe.RecipeResponse;
import com.whatsonmyplate.dto.recipe.RecipeUpdateRequest;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.entity.model.IngredientItem;
import com.whatsonmyplate.entity.model.InstructionStep;
import com.whatsonmyplate.exception.ResourceNotFoundException;
import com.whatsonmyplate.repository.RecipeRepository;
import com.whatsonmyplate.service.ai.AiRecipeService;
import com.whatsonmyplate.service.external.TheMealDbClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private TheMealDbClient theMealDbClient;

    @Mock
    private AiRecipeService aiRecipeService;

    private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        recipeService = new RecipeService(recipeRepository, theMealDbClient, aiRecipeService);
    }

    @Test
    void testCreateRecipeSuccess() {
        RecipeCreateRequest request = new RecipeCreateRequest(
                "Chicken Curry",
                "Delicious spicy chicken curry",
                RecipeSource.USER_CREATED,
                false,
                15,
                30,
                4,
                SpiceLevel.SPICY,
                List.of(new IngredientItemDto("Chicken", 500.0, "g")),
                List.of(new InstructionStepDto(1, "Cook chicken with spices"))
        );

        Recipe savedEntity = new Recipe();
        savedEntity.setId(1L);
        savedEntity.setTitle("Chicken Curry");
        savedEntity.setDescription("Delicious spicy chicken curry");
        savedEntity.setSource(RecipeSource.USER_CREATED);
        savedEntity.setIsAiGenerated(false);
        savedEntity.setPrepTimeMinutes(15);
        savedEntity.setCookTimeMinutes(30);
        savedEntity.setServings(4);
        savedEntity.setSpiceLevel(SpiceLevel.SPICY);
        savedEntity.setIngredients(List.of(new IngredientItem("Chicken", 500.0, "g")));
        savedEntity.setInstructions(List.of(new InstructionStep(1, "Cook chicken with spices")));
        savedEntity.setCreatedAt(LocalDateTime.now());
        savedEntity.setUpdatedAt(LocalDateTime.now());

        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedEntity);

        RecipeResponse response = recipeService.createRecipe(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Chicken Curry", response.getTitle());
        assertEquals(SpiceLevel.SPICY, response.getSpiceLevel());
        assertEquals(1, response.getIngredients().size());
        assertEquals("Chicken", response.getIngredients().get(0).getName());
        assertEquals(1, response.getInstructions().size());
        assertEquals(1, response.getInstructions().get(0).getStep());

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(captor.capture());
        assertEquals("Chicken Curry", captor.getValue().getTitle());
    }

    @Test
    void testGetRecipeByIdFound() {
        Recipe recipe = new Recipe();
        recipe.setId(10L);
        recipe.setTitle("Paneer Tikka");
        recipe.setSource(RecipeSource.USER_CREATED);
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));

        RecipeResponse response = recipeService.getRecipeById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Paneer Tikka", response.getTitle());
    }

    @Test
    void testGetRecipeByIdNotFound() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recipeService.getRecipeById(99L));
    }

    @Test
    void testListRecipesSuccessCacheHit() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Biryani");
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());

        Page<Recipe> pageResult = new PageImpl<>(List.of(recipe));
        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(pageResult);

        RecipePageResponse response = recipeService.listRecipes("Biryani", null, null, null, 0, 12, "title,asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Biryani", response.getContent().get(0).getTitle());
        verify(theMealDbClient, never()).searchRecipes(anyString());
        verify(aiRecipeService, never()).generateRecipeFromText(anyString());
    }

    @Test
    void testListRecipesCacheMissTheMealDbHit() {
        Recipe foundMeal = new Recipe();
        foundMeal.setId(20L);
        foundMeal.setTitle("Chicken Teriyaki");
        foundMeal.setSource(RecipeSource.THE_MEAL_DB);
        foundMeal.setCreatedAt(LocalDateTime.now());
        foundMeal.setUpdatedAt(LocalDateTime.now());

        // First DB lookup empty, second returns the saved meal
        Page<Recipe> emptyPage = new PageImpl<>(List.of());
        Page<Recipe> filledPage = new PageImpl<>(List.of(foundMeal));

        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage)
                .thenReturn(filledPage);

        when(theMealDbClient.searchRecipes("Teriyaki")).thenReturn(List.of(foundMeal));

        RecipePageResponse response = recipeService.listRecipes("Teriyaki", null, null, null, 0, 12, "createdAt,desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Chicken Teriyaki", response.getContent().get(0).getTitle());
        verify(recipeRepository).saveAll(anyList());
    }

    @Test
    void testListRecipesCacheMissGeminiAiFallback() {
        // First DB lookup empty, second returns the AI recipe
        Page<Recipe> emptyPage = new PageImpl<>(List.of());

        Recipe aiRecipe = new Recipe();
        aiRecipe.setId(30L);
        aiRecipe.setTitle("Chicken Biryani");
        aiRecipe.setSource(RecipeSource.AI_GENERATED);
        aiRecipe.setIsAiGenerated(true);
        aiRecipe.setCreatedAt(LocalDateTime.now());
        aiRecipe.setUpdatedAt(LocalDateTime.now());

        Page<Recipe> filledPage = new PageImpl<>(List.of(aiRecipe));

        when(recipeRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage)
                .thenReturn(filledPage);

        when(theMealDbClient.searchRecipes("Biryani")).thenReturn(List.of());

        AiRecipeResult aiResult = new AiRecipeResult();
        aiResult.setDishName("Chicken Biryani");
        aiResult.setDescription("Classic aromatic Indian rice dish");
        aiResult.setPrepTimeMinutes(20);
        aiResult.setCookTimeMinutes(40);
        aiResult.setServings(4);
        aiResult.setSpiceLevel(SpiceLevel.SPICY);
        aiResult.setConfidence(0.95);
        aiResult.setIngredients(List.of(new AiIngredient("Chicken", 500.0, "g")));
        aiResult.setInstructions(List.of(new AiInstruction(1, "Layer spiced chicken with basmati rice")));

        when(aiRecipeService.generateRecipeFromText(anyString())).thenReturn(aiResult);

        RecipePageResponse response = recipeService.listRecipes("Biryani", null, null, null, 0, 12, "createdAt,desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Chicken Biryani", response.getContent().get(0).getTitle());
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void testListRecipesNegativePageThrowsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.listRecipes(-1, 20, "createdAt,desc")
        );
        assertEquals("Page index must not be less than zero.", ex.getMessage());
    }

    @Test
    void testListRecipesZeroOrExcessiveSizeThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.listRecipes(0, 0, "createdAt,desc")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.listRecipes(0, 51, "createdAt,desc")
        );
    }

    @Test
    void testListRecipesInvalidSortFieldThrowsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.listRecipes(0, 20, "invalidField,desc")
        );
        assertTrue(ex.getMessage().contains("Invalid sort field"));
    }

    @Test
    void testListRecipesInvalidSourceThrowsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.listRecipes("chicken", "INVALID_SOURCE", "SPICY", 25, 0, 12, "createdAt,desc")
        );
        assertTrue(ex.getMessage().contains("Invalid source"));
    }

    @Test
    void testListRecipesInvalidSpiceLevelThrowsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.listRecipes("chicken", "AI_GENERATED", "EXTRA_HOT", 25, 0, 12, "createdAt,desc")
        );
        assertTrue(ex.getMessage().contains("Invalid spiceLevel"));
    }

    @Test
    void testListRecipesNegativeMaxPrepTimeThrowsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.listRecipes("chicken", "AI_GENERATED", "SPICY", -5, 0, 12, "createdAt,desc")
        );
        assertEquals("maxPrepTime must be greater than or equal to 0.", ex.getMessage());
    }

    @Test
    void testUpdateRecipeFound() {
        Recipe existing = new Recipe();
        existing.setId(5L);
        existing.setTitle("Old Title");
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        RecipeUpdateRequest request = new RecipeUpdateRequest(
                "New Title",
                "New Description",
                "https://example.com/image.jpg",
                RecipeSource.USER_CREATED,
                null,
                false,
                20,
                40,
                2,
                SpiceLevel.MILD,
                List.of(new IngredientItemDto("Tofu", 200.0, "g")),
                List.of(new InstructionStepDto(1, "Fry tofu"))
        );

        when(recipeRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecipeResponse response = recipeService.updateRecipe(5L, request);

        assertNotNull(response);
        assertEquals("New Title", response.getTitle());
        assertEquals(SpiceLevel.MILD, response.getSpiceLevel());
        assertEquals(1, response.getIngredients().size());
    }

    @Test
    void testUpdateRecipeNotFound() {
        RecipeUpdateRequest request = new RecipeUpdateRequest();
        request.setTitle("Title");
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recipeService.updateRecipe(99L, request));
    }

    @Test
    void testDeleteRecipeFound() {
        Recipe recipe = new Recipe();
        recipe.setId(7L);
        when(recipeRepository.findById(7L)).thenReturn(Optional.of(recipe));

        recipeService.deleteRecipe(7L);

        verify(recipeRepository).delete(recipe);
    }

    @Test
    void testDeleteRecipeNotFound() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recipeService.deleteRecipe(99L));
    }

    @Test
    void testDeleteRecipeConstraintViolationThrowsCleanException() {
        Recipe recipe = new Recipe();
        recipe.setId(7L);
        when(recipeRepository.findById(7L)).thenReturn(Optional.of(recipe));
        doThrow(new DataIntegrityViolationException("Constraint violation")).when(recipeRepository).delete(recipe);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> recipeService.deleteRecipe(7L)
        );
        assertEquals("Cannot delete recipe because it is currently referenced by other records.", ex.getMessage());
    }
}
