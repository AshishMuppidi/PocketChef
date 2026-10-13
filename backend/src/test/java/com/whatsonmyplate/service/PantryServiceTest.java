package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.pantry.PantryItemCreateRequest;
import com.whatsonmyplate.dto.pantry.PantryItemResponse;
import com.whatsonmyplate.dto.pantry.PantryItemUpdateRequest;
import com.whatsonmyplate.dto.pantry.PantryRecipeResponse;
import com.whatsonmyplate.entity.PantryItem;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.User;
import com.whatsonmyplate.entity.model.IngredientItem;
import com.whatsonmyplate.exception.ResourceNotFoundException;
import com.whatsonmyplate.repository.PantryItemRepository;
import com.whatsonmyplate.repository.RecipeRepository;
import com.whatsonmyplate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PantryServiceTest {

    @Mock
    private PantryItemRepository pantryItemRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    private PantryService pantryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        pantryService = new PantryService(pantryItemRepository, recipeRepository, userRepository);
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setUsername("testuser");
    }

    @Test
    void testGetPantryItemsSuccess() {
        PantryItem item = new PantryItem(testUser, "Chicken Breast", 2.0, "pieces", LocalDate.now().plusDays(5));
        item.setId(10L);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(pantryItemRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of(item));

        List<PantryItemResponse> result = pantryService.getPantryItems("user@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Chicken Breast", result.get(0).getName());
        assertEquals(2.0, result.get(0).getQuantity());
    }

    @Test
    void testCreatePantryItemNew() {
        PantryItemCreateRequest request = new PantryItemCreateRequest("Garlic", 3.0, "cloves", null);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(pantryItemRepository.findByUserIdAndNameIgnoreCase(1L, "Garlic")).thenReturn(Optional.empty());

        PantryItem saved = new PantryItem(testUser, "Garlic", 3.0, "cloves", null);
        saved.setId(11L);
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(LocalDateTime.now());

        when(pantryItemRepository.save(any(PantryItem.class))).thenReturn(saved);

        PantryItemResponse response = pantryService.createPantryItem("user@example.com", request);

        assertNotNull(response);
        assertEquals("Garlic", response.getName());
        assertEquals(3.0, response.getQuantity());
    }

    @Test
    void testCreatePantryItemDuplicateMergesQuantity() {
        PantryItemCreateRequest request = new PantryItemCreateRequest("Garlic", 2.0, "cloves", null);

        PantryItem existing = new PantryItem(testUser, "Garlic", 3.0, "cloves", null);
        existing.setId(11L);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(pantryItemRepository.findByUserIdAndNameIgnoreCase(1L, "Garlic")).thenReturn(Optional.of(existing));
        when(pantryItemRepository.save(any(PantryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        PantryItemResponse response = pantryService.createPantryItem("user@example.com", request);

        assertNotNull(response);
        assertEquals(5.0, response.getQuantity());
    }

    @Test
    void testUpdatePantryItemFound() {
        PantryItem existing = new PantryItem(testUser, "Rice", 1.0, "kg", null);
        existing.setId(12L);
        existing.setCreatedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());

        PantryItemUpdateRequest updateReq = new PantryItemUpdateRequest("Brown Rice", 2.0, "kg", null);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(pantryItemRepository.findByIdAndUserId(12L, 1L)).thenReturn(Optional.of(existing));
        when(pantryItemRepository.save(any(PantryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        PantryItemResponse response = pantryService.updatePantryItem("user@example.com", 12L, updateReq);

        assertNotNull(response);
        assertEquals("Brown Rice", response.getName());
        assertEquals(2.0, response.getQuantity());
    }

    @Test
    void testUpdatePantryItemNotFound() {
        PantryItemUpdateRequest updateReq = new PantryItemUpdateRequest("Rice", 1.0, "kg", null);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(pantryItemRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> pantryService.updatePantryItem("user@example.com", 99L, updateReq));
    }

    @Test
    void testDeletePantryItemSuccess() {
        PantryItem existing = new PantryItem(testUser, "Onion", 2.0, "pieces", null);
        existing.setId(14L);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(pantryItemRepository.findByIdAndUserId(14L, 1L)).thenReturn(Optional.of(existing));

        pantryService.deletePantryItem("user@example.com", 14L);

        verify(pantryItemRepository).delete(existing);
    }

    @Test
    void testFindMatchingRecipes() {
        PantryItem item1 = new PantryItem(testUser, "Chicken Breast", 1.0, "kg", null);
        PantryItem item2 = new PantryItem(testUser, "Rice", 500.0, "g", null);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(pantryItemRepository.findByUserId(1L)).thenReturn(List.of(item1, item2));

        Recipe recipe1 = new Recipe();
        recipe1.setId(101L);
        recipe1.setTitle("Chicken Rice Bowl");
        recipe1.setIngredients(List.of(
                new IngredientItem("Chicken Breast", 500.0, "g"),
                new IngredientItem("White Rice", 1.0, "cup")
        ));
        recipe1.setCreatedAt(LocalDateTime.now());
        recipe1.setUpdatedAt(LocalDateTime.now());

        Recipe recipe2 = new Recipe();
        recipe2.setId(102L);
        recipe2.setTitle("Beef Tacos");
        recipe2.setIngredients(List.of(
                new IngredientItem("Ground Beef", 300.0, "g"),
                new IngredientItem("Taco Shells", 6.0, "pieces")
        ));
        recipe2.setCreatedAt(LocalDateTime.now());
        recipe2.setUpdatedAt(LocalDateTime.now());

        when(recipeRepository.findAll()).thenReturn(List.of(recipe1, recipe2));

        List<PantryRecipeResponse> result = pantryService.findMatchingRecipes("user@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Chicken Rice Bowl", result.get(0).getRecipe().getTitle());
        assertEquals(2, result.get(0).getMatchedIngredients());
        assertEquals(100, result.get(0).getMatchPercentage());
    }
}
