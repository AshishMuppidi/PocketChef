package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.pantry.PantryItemCreateRequest;
import com.whatsonmyplate.dto.pantry.PantryItemResponse;
import com.whatsonmyplate.dto.pantry.PantryItemUpdateRequest;
import com.whatsonmyplate.dto.pantry.PantryRecipeResponse;
import com.whatsonmyplate.dto.recipe.IngredientItemDto;
import com.whatsonmyplate.dto.recipe.InstructionStepDto;
import com.whatsonmyplate.dto.recipe.RecipeResponse;
import com.whatsonmyplate.entity.PantryItem;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.User;
import com.whatsonmyplate.entity.model.IngredientItem;
import com.whatsonmyplate.exception.ResourceNotFoundException;
import com.whatsonmyplate.repository.PantryItemRepository;
import com.whatsonmyplate.repository.RecipeRepository;
import com.whatsonmyplate.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PantryService {

    private final PantryItemRepository pantryItemRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public PantryService(
            PantryItemRepository pantryItemRepository,
            RecipeRepository recipeRepository,
            UserRepository userRepository
    ) {
        this.pantryItemRepository = pantryItemRepository;
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PantryItemResponse> getPantryItems(String principalUsername) {
        User user = getAuthenticatedUser(principalUsername);
        return pantryItemRepository.findByUserIdOrderByNameAsc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PantryItemResponse createPantryItem(String principalUsername, PantryItemCreateRequest request) {
        User user = getAuthenticatedUser(principalUsername);
        String trimmedName = request.getName().trim();

        // Check for existing ingredient for this user to avoid duplicate rows
        Optional<PantryItem> existingOpt = pantryItemRepository.findByUserIdAndNameIgnoreCase(user.getId(), trimmedName);

        if (existingOpt.isPresent()) {
            PantryItem existing = existingOpt.get();
            if (request.getQuantity() != null) {
                double currentQty = existing.getQuantity() != null ? existing.getQuantity() : 0.0;
                existing.setQuantity(currentQty + request.getQuantity());
            }
            if (request.getUnit() != null && !request.getUnit().isBlank()) {
                existing.setUnit(request.getUnit().trim());
            }
            if (request.getExpirationDate() != null) {
                existing.setExpirationDate(request.getExpirationDate());
            }
            PantryItem updated = pantryItemRepository.save(existing);
            return mapToResponse(updated);
        }

        PantryItem newItem = new PantryItem(
                user,
                trimmedName,
                request.getQuantity(),
                request.getUnit() != null ? request.getUnit().trim() : null,
                request.getExpirationDate()
        );

        PantryItem saved = pantryItemRepository.save(newItem);
        return mapToResponse(saved);
    }

    @Transactional
    public PantryItemResponse updatePantryItem(String principalUsername, Long id, PantryItemUpdateRequest request) {
        User user = getAuthenticatedUser(principalUsername);
        PantryItem item = pantryItemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pantry item not found with id: " + id));

        item.setName(request.getName().trim());
        item.setQuantity(request.getQuantity());
        item.setUnit(request.getUnit() != null ? request.getUnit().trim() : null);
        item.setExpirationDate(request.getExpirationDate());

        PantryItem updated = pantryItemRepository.save(item);
        return mapToResponse(updated);
    }

    @Transactional
    public void deletePantryItem(String principalUsername, Long id) {
        User user = getAuthenticatedUser(principalUsername);
        PantryItem item = pantryItemRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pantry item not found with id: " + id));

        pantryItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<PantryRecipeResponse> findMatchingRecipes(String principalUsername) {
        User user = getAuthenticatedUser(principalUsername);
        List<PantryItem> pantryItems = pantryItemRepository.findByUserId(user.getId());

        if (pantryItems.isEmpty()) {
            return Collections.emptyList();
        }

        // Build set of normalized pantry ingredient names
        Set<String> normalizedPantryNames = pantryItems.stream()
                .map(item -> normalizeIngredient(item.getName()))
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());

        List<Recipe> allRecipes = recipeRepository.findAll();
        List<PantryRecipeResponse> matches = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            List<IngredientItem> ingredients = recipe.getIngredients();
            if (ingredients == null || ingredients.isEmpty()) {
                continue;
            }

            int matchedCount = 0;
            for (IngredientItem ingredient : ingredients) {
                String normalizedRecipeIngredient = normalizeIngredient(ingredient.getName());
                if (isIngredientMatch(normalizedRecipeIngredient, normalizedPantryNames)) {
                    matchedCount++;
                }
            }

            if (matchedCount > 0) {
                int totalIngredients = ingredients.size();
                int missingCount = Math.max(0, totalIngredients - matchedCount);
                int matchPercentage = (int) Math.round(((double) matchedCount / totalIngredients) * 100);

                matches.add(new PantryRecipeResponse(
                        mapToRecipeResponse(recipe),
                        matchedCount,
                        totalIngredients,
                        missingCount,
                        matchPercentage
                ));
            }
        }

        // Sort by match percentage DESC, matched ingredients DESC
        matches.sort(
                Comparator.comparingInt(PantryRecipeResponse::getMatchPercentage).reversed()
                        .thenComparing(Comparator.comparingInt(PantryRecipeResponse::getMatchedIngredients).reversed())
        );

        return matches;
    }

    private boolean isIngredientMatch(String recipeIngredient, Set<String> pantryIngredients) {
        for (String pantryItem : pantryIngredients) {
            if (recipeIngredient.contains(pantryItem) || pantryItem.contains(recipeIngredient)) {
                return true;
            }
            // Check individual token matching
            for (String word : pantryItem.split("\\s+")) {
                if (word.length() > 2 && recipeIngredient.contains(word)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizeIngredient(String name) {
        if (name == null) return "";
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private User getAuthenticatedUser(String principalUsername) {
        return userRepository.findByEmail(principalUsername)
                .or(() -> userRepository.findByUsername(principalUsername))
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));
    }

    private PantryItemResponse mapToResponse(PantryItem item) {
        return new PantryItemResponse(
                item.getId(),
                item.getName(),
                item.getQuantity(),
                item.getUnit(),
                item.getExpirationDate(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private RecipeResponse mapToRecipeResponse(Recipe recipe) {
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
