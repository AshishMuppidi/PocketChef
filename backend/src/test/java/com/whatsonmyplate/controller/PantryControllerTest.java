package com.whatsonmyplate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsonmyplate.dto.pantry.PantryItemCreateRequest;
import com.whatsonmyplate.dto.pantry.PantryItemResponse;
import com.whatsonmyplate.dto.pantry.PantryItemUpdateRequest;
import com.whatsonmyplate.dto.pantry.PantryRecipeResponse;
import com.whatsonmyplate.dto.recipe.RecipeResponse;
import com.whatsonmyplate.exception.ResourceNotFoundException;
import com.whatsonmyplate.service.PantryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PantryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PantryService pantryService;

    @Test
    void testGetPantryUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/pantry")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testGetPantryAuthenticatedSuccess() throws Exception {
        PantryItemResponse item = new PantryItemResponse(
                1L,
                "Chicken",
                2.0,
                "kg",
                LocalDate.now().plusDays(3),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(pantryService.getPantryItems("user@example.com")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/pantry")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Chicken"))
                .andExpect(jsonPath("$[0].quantity").value(2.0));
    }

    @Test
    void testCreatePantryItemUnauthenticatedReturns401() throws Exception {
        PantryItemCreateRequest req = new PantryItemCreateRequest("Flour", 1.0, "kg", null);

        mockMvc.perform(post("/api/pantry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testCreatePantryItemAuthenticatedSuccess() throws Exception {
        PantryItemCreateRequest req = new PantryItemCreateRequest("Flour", 1.0, "kg", null);
        PantryItemResponse res = new PantryItemResponse(
                2L,
                "Flour",
                1.0,
                "kg",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(pantryService.createPantryItem(eq("user@example.com"), any(PantryItemCreateRequest.class)))
                .thenReturn(res);

        mockMvc.perform(post("/api/pantry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Flour"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testCreatePantryItemValidationBlankNameReturns400() throws Exception {
        PantryItemCreateRequest req = new PantryItemCreateRequest("", 1.0, "kg", null);

        mockMvc.perform(post("/api/pantry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testUpdatePantryItemSuccess() throws Exception {
        PantryItemUpdateRequest req = new PantryItemUpdateRequest("Organic Flour", 2.0, "kg", null);
        PantryItemResponse res = new PantryItemResponse(
                2L,
                "Organic Flour",
                2.0,
                "kg",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(pantryService.updatePantryItem(eq("user@example.com"), eq(2L), any(PantryItemUpdateRequest.class)))
                .thenReturn(res);

        mockMvc.perform(put("/api/pantry/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Organic Flour"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testDeletePantryItemSuccess() throws Exception {
        doNothing().when(pantryService).deletePantryItem("user@example.com", 2L);

        mockMvc.perform(delete("/api/pantry/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testDeletePantryItemNotFoundReturns404() throws Exception {
        doThrow(new ResourceNotFoundException("Pantry item not found with id: 99"))
                .when(pantryService).deletePantryItem("user@example.com", 99L);

        mockMvc.perform(delete("/api/pantry/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testGetPantryRecipesSuccess() throws Exception {
        RecipeResponse recipe = new RecipeResponse();
        recipe.setId(10L);
        recipe.setTitle("Tacos");

        PantryRecipeResponse pantryRecipe = new PantryRecipeResponse(
                recipe,
                4,
                5,
                1,
                80
        );

        when(pantryService.findMatchingRecipes("user@example.com")).thenReturn(List.of(pantryRecipe));

        mockMvc.perform(get("/api/pantry/recipes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipe.title").value("Tacos"))
                .andExpect(jsonPath("$[0].matchedIngredients").value(4))
                .andExpect(jsonPath("$[0].matchPercentage").value(80));
    }
}
