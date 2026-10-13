package com.whatsonmyplate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsonmyplate.dto.ai.AdjustedIngredient;
import com.whatsonmyplate.dto.ai.IngredientSubstitutionRequest;
import com.whatsonmyplate.dto.ai.IngredientSubstitutionResult;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentRequest;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentResult;
import com.whatsonmyplate.dto.recipe.IngredientItemDto;
import com.whatsonmyplate.dto.recipe.InstructionStepDto;
import com.whatsonmyplate.dto.recipe.RecipeCreateRequest;
import com.whatsonmyplate.dto.recipe.RecipePageResponse;
import com.whatsonmyplate.dto.recipe.RecipeResponse;
import com.whatsonmyplate.dto.recipe.RecipeUpdateRequest;
import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.exception.AiServiceException;
import com.whatsonmyplate.exception.ResourceNotFoundException;
import com.whatsonmyplate.service.ImageRecipeService;
import com.whatsonmyplate.service.RecipeAiAdjustmentService;
import com.whatsonmyplate.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecipeService recipeService;

    @MockBean
    private ImageRecipeService imageRecipeService;

    @MockBean
    private RecipeAiAdjustmentService recipeAiAdjustmentService;

    @Test
    void testListRecipesPublic() throws Exception {
        RecipeResponse item = new RecipeResponse(
                1L,
                "Biryani",
                "Spicy rice dish",
                "https://example.com/biryani.jpg",
                RecipeSource.USER_CREATED,
                null,
                false,
                20,
                40,
                4,
                SpiceLevel.SPICY,
                List.of(new IngredientItemDto("Rice", 2.0, "cups")),
                List.of(new InstructionStepDto(1, "Cook rice with spices")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        RecipePageResponse pageResponse = new RecipePageResponse(
                List.of(item),
                0,
                12,
                1L,
                1,
                true
        );

        when(recipeService.listRecipes(null, null, null, null, 0, 12, "createdAt,desc")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Biryani"))
                .andExpect(jsonPath("$.totalElements").value(1L));
    }

    @Test
    void testListRecipesWithSearchAndFilterParams() throws Exception {
        RecipeResponse item = new RecipeResponse(
                1L,
                "Spicy Chicken Curry",
                "Spicy curry",
                null,
                RecipeSource.AI_GENERATED,
                null,
                true,
                15,
                25,
                4,
                SpiceLevel.SPICY,
                List.of(),
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        RecipePageResponse pageResponse = new RecipePageResponse(
                List.of(item),
                0,
                12,
                1L,
                1,
                true
        );

        when(recipeService.listRecipes("chicken", "AI_GENERATED", "SPICY", 25, 0, 12, "createdAt,desc"))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/recipes?q=chicken&source=AI_GENERATED&spiceLevel=SPICY&maxPrepTime=25")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Spicy Chicken Curry"))
                .andExpect(jsonPath("$.totalElements").value(1L));
    }

    @Test
    void testListRecipesInvalidPaginationParamsReturns400() throws Exception {
        when(recipeService.listRecipes(null, null, null, null, -1, 12, "createdAt,desc"))
                .thenThrow(new IllegalArgumentException("Page index must not be less than zero."));

        mockMvc.perform(get("/api/recipes?page=-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page index must not be less than zero."));
    }

    @Test
    void testListRecipesInvalidFilterReturns400() throws Exception {
        when(recipeService.listRecipes(null, "INVALID_SRC", null, null, 0, 12, "createdAt,desc"))
                .thenThrow(new IllegalArgumentException("Invalid source: 'INVALID_SRC'."));

        mockMvc.perform(get("/api/recipes?source=INVALID_SRC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid source: 'INVALID_SRC'."));
    }

    @Test
    void testGetRecipeByIdPublicFound() throws Exception {
        RecipeResponse item = new RecipeResponse(
                2L,
                "Salad",
                "Healthy salad",
                null,
                RecipeSource.USER_CREATED,
                null,
                false,
                10,
                0,
                2,
                SpiceLevel.MILD,
                List.of(new IngredientItemDto("Lettuce", 1.0, "head")),
                List.of(new InstructionStepDto(1, "Chop lettuce")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(recipeService.getRecipeById(2L)).thenReturn(item);

        mockMvc.perform(get("/api/recipes/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.title").value("Salad"));
    }

    @Test
    void testGetRecipeByIdPublicNotFound() throws Exception {
        when(recipeService.getRecipeById(99L)).thenThrow(new ResourceNotFoundException("Recipe not found with id: 99"));

        mockMvc.perform(get("/api/recipes/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Recipe not found with id: 99"));
    }

    @Test
    void testCreateRecipeUnauthenticatedFails() throws Exception {
        RecipeCreateRequest request = new RecipeCreateRequest(
                "Recipe",
                "Desc",
                RecipeSource.USER_CREATED,
                false,
                10,
                20,
                2,
                SpiceLevel.MILD,
                List.of(new IngredientItemDto("Salt", 1.0, "tsp")),
                List.of(new InstructionStepDto(1, "Add salt"))
        );

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testCreateRecipeAuthenticatedSucceeds() throws Exception {
        RecipeCreateRequest request = new RecipeCreateRequest(
                "Pasta",
                "Creamy pasta",
                RecipeSource.USER_CREATED,
                false,
                10,
                15,
                2,
                SpiceLevel.MILD,
                List.of(new IngredientItemDto("Pasta", 200.0, "g")),
                List.of(new InstructionStepDto(1, "Boil pasta"))
        );

        RecipeResponse response = new RecipeResponse(
                3L,
                "Pasta",
                "Creamy pasta",
                null,
                RecipeSource.USER_CREATED,
                null,
                false,
                10,
                15,
                2,
                SpiceLevel.MILD,
                List.of(new IngredientItemDto("Pasta", 200.0, "g")),
                List.of(new InstructionStepDto(1, "Boil pasta")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(recipeService.createRecipe(any(RecipeCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.title").value("Pasta"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testCreateRecipeValidationFailureNestedIngredients() throws Exception {
        RecipeCreateRequest request = new RecipeCreateRequest(
                "Pasta",
                "Desc",
                RecipeSource.USER_CREATED,
                false,
                10,
                15,
                2,
                SpiceLevel.MILD,
                List.of(new IngredientItemDto("", -5.0, "")),
                List.of(new InstructionStepDto(1, "Boil pasta"))
        );

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testCreateRecipeInvalidEnumReturns400() throws Exception {
        String invalidJson = """
                {
                  "title": "Pasta",
                  "source": "INVALID_SOURCE_VALUE",
                  "ingredients": [{"name": "Pasta", "quantity": 100, "unit": "g"}],
                  "instructions": [{"step": 1, "text": "Cook"}]
                }
                """;

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testUpdateRecipeUnauthenticatedFails() throws Exception {
        RecipeUpdateRequest request = new RecipeUpdateRequest();
        request.setTitle("Updated Title");

        mockMvc.perform(put("/api/recipes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testUpdateRecipeAuthenticatedSucceeds() throws Exception {
        RecipeUpdateRequest request = new RecipeUpdateRequest(
                "Updated Pasta",
                "Updated Creamy pasta",
                null,
                RecipeSource.USER_CREATED,
                null,
                false,
                12,
                18,
                3,
                SpiceLevel.MEDIUM,
                List.of(new IngredientItemDto("Pasta", 250.0, "g")),
                List.of(new InstructionStepDto(1, "Boil pasta with salt"))
        );

        RecipeResponse response = new RecipeResponse(
                1L,
                "Updated Pasta",
                "Updated Creamy pasta",
                null,
                RecipeSource.USER_CREATED,
                null,
                false,
                12,
                18,
                3,
                SpiceLevel.MEDIUM,
                List.of(new IngredientItemDto("Pasta", 250.0, "g")),
                List.of(new InstructionStepDto(1, "Boil pasta with salt")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(recipeService.updateRecipe(eq(1L), any(RecipeUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/recipes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Pasta"))
                .andExpect(jsonPath("$.spiceLevel").value("MEDIUM"));
    }

    @Test
    void testDeleteRecipeUnauthenticatedFails() throws Exception {
        mockMvc.perform(delete("/api/recipes/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testDeleteRecipeAuthenticatedSucceeds() throws Exception {
        doNothing().when(recipeService).deleteRecipe(1L);

        mockMvc.perform(delete("/api/recipes/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testDeleteRecipeNotFoundReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Recipe not found with id: 99"))
                .when(recipeService).deleteRecipe(99L);

        mockMvc.perform(delete("/api/recipes/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // --- Image-to-Recipe Endpoint Tests ---

    @Test
    void testAnalyzeDishImageUnauthenticatedReturns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "food.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/api/recipes/analyze-image")
                .file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testAnalyzeDishImageAuthenticatedSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "curry.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4}
        );

        RecipeResponse response = new RecipeResponse(
                10L,
                "Chicken Tikka Masala",
                "Rich and creamy chicken curry",
                null,
                RecipeSource.AI_GENERATED,
                null,
                true,
                15,
                25,
                4,
                SpiceLevel.MEDIUM,
                List.of(new IngredientItemDto("Chicken", 500.0, "g")),
                List.of(new InstructionStepDto(1, "Marinate and grill chicken")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(imageRecipeService.analyzeDishImage(any(MultipartFile.class), any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/recipes/analyze-image")
                .file(file)
                .param("promptNotes", "Mild spicy please"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Chicken Tikka Masala"))
                .andExpect(jsonPath("$.source").value("AI_GENERATED"))
                .andExpect(jsonPath("$.isAiGenerated").value(true));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testAnalyzeDishImageInvalidMimeTypeReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "recipe.pdf",
                "application/pdf",
                new byte[]{1, 2, 3}
        );

        when(imageRecipeService.analyzeDishImage(any(MultipartFile.class), any()))
                .thenThrow(new IllegalArgumentException("Unsupported image type: 'application/pdf'. Allowed types are: image/jpeg, image/png, image/webp."));

        mockMvc.perform(multipart("/api/recipes/analyze-image")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported image type: 'application/pdf'. Allowed types are: image/jpeg, image/png, image/webp."));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testAnalyzeDishImageAiFailureReturns503() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "food.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        when(imageRecipeService.analyzeDishImage(any(MultipartFile.class), any()))
                .thenThrow(new AiServiceException("Google Gemini API is unavailable"));

        mockMvc.perform(multipart("/api/recipes/analyze-image")
                .file(file))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Google Gemini API is unavailable"));
    }

    // --- Smart Ingredient Substitution Tests ---

    @Test
    void testSubstitutionUnauthenticatedReturns401() throws Exception {
        IngredientSubstitutionRequest req = new IngredientSubstitutionRequest("Heavy Cream", null);

        mockMvc.perform(post("/api/recipes/10/substitutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testSubstitutionAuthenticatedSuccess() throws Exception {
        IngredientSubstitutionRequest req = new IngredientSubstitutionRequest("Heavy Cream", "No dairy");
        IngredientSubstitutionResult res = new IngredientSubstitutionResult(
                "Heavy Cream", "Coconut Cream", "1:1 ratio", "Rich vegan fat", "Use equal parts coconut cream", 0.90
        );

        when(recipeAiAdjustmentService.getIngredientSubstitution(eq(10L), any(IngredientSubstitutionRequest.class)))
                .thenReturn(res);

        mockMvc.perform(post("/api/recipes/10/substitutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.substitute").value("Coconut Cream"))
                .andExpect(jsonPath("$.confidence").value(0.90));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testSubstitutionBlankIngredientReturns400() throws Exception {
        IngredientSubstitutionRequest req = new IngredientSubstitutionRequest("", null);

        mockMvc.perform(post("/api/recipes/10/substitutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // --- Spice Level Adjustment Tests ---

    @Test
    void testSpiceAdjustmentUnauthenticatedReturns401() throws Exception {
        SpiceAdjustmentRequest req = new SpiceAdjustmentRequest("MILD");

        mockMvc.perform(post("/api/recipes/10/spice-adjustment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testSpiceAdjustmentAuthenticatedSuccess() throws Exception {
        SpiceAdjustmentRequest req = new SpiceAdjustmentRequest("MILD");
        SpiceAdjustmentResult res = new SpiceAdjustmentResult(
                10L, "SPICY", "MILD",
                List.of(new AdjustedIngredient("Chili Flakes", 2.0, "tsp", 0.5, "tsp", "Reduced heat")),
                "Mild version preserves flavor", 0.95
        );

        when(recipeAiAdjustmentService.getSpiceAdjustment(eq(10L), any(SpiceAdjustmentRequest.class)))
                .thenReturn(res);

        mockMvc.perform(post("/api/recipes/10/spice-adjustment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetSpiceLevel").value("MILD"))
                .andExpect(jsonPath("$.adjustedIngredients[0].name").value("Chili Flakes"))
                .andExpect(jsonPath("$.adjustedIngredients[0].adjustedQuantity").value(0.5));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testSpiceAdjustmentInvalidPatternReturns400() throws Exception {
        SpiceAdjustmentRequest req = new SpiceAdjustmentRequest("SUPER_HOT");

        mockMvc.perform(post("/api/recipes/10/spice-adjustment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
