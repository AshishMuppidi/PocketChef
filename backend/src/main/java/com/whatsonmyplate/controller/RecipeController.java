package com.whatsonmyplate.controller;

import com.whatsonmyplate.dto.ai.IngredientSubstitutionRequest;
import com.whatsonmyplate.dto.ai.IngredientSubstitutionResult;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentRequest;
import com.whatsonmyplate.dto.ai.SpiceAdjustmentResult;
import com.whatsonmyplate.dto.recipe.RecipeCreateRequest;
import com.whatsonmyplate.dto.recipe.RecipePageResponse;
import com.whatsonmyplate.dto.recipe.RecipeResponse;
import com.whatsonmyplate.dto.recipe.RecipeUpdateRequest;
import com.whatsonmyplate.service.ImageRecipeService;
import com.whatsonmyplate.service.RecipeAiAdjustmentService;
import com.whatsonmyplate.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final ImageRecipeService imageRecipeService;
    private final RecipeAiAdjustmentService recipeAiAdjustmentService;

    public RecipeController(
            RecipeService recipeService,
            ImageRecipeService imageRecipeService,
            RecipeAiAdjustmentService recipeAiAdjustmentService
    ) {
        this.recipeService = recipeService;
        this.imageRecipeService = imageRecipeService;
        this.recipeAiAdjustmentService = recipeAiAdjustmentService;
    }

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody RecipeCreateRequest request) {
        RecipeResponse response = recipeService.createRecipe(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> analyzeDishImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "promptNotes", required = false) String promptNotes
    ) {
        RecipeResponse response = imageRecipeService.analyzeDishImage(image, promptNotes);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getRecipeById(@PathVariable Long id) {
        RecipeResponse response = recipeService.getRecipeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<RecipePageResponse> listRecipes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String spiceLevel,
            @RequestParam(required = false) Integer maxPrepTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        RecipePageResponse response = recipeService.listRecipes(q, source, spiceLevel, maxPrepTime, page, size, sort);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeUpdateRequest request
    ) {
        RecipeResponse response = recipeService.updateRecipe(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/substitutions")
    public ResponseEntity<IngredientSubstitutionResult> getIngredientSubstitution(
            @PathVariable Long id,
            @Valid @RequestBody IngredientSubstitutionRequest request
    ) {
        IngredientSubstitutionResult response = recipeAiAdjustmentService.getIngredientSubstitution(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/spice-adjustment")
    public ResponseEntity<SpiceAdjustmentResult> getSpiceAdjustment(
            @PathVariable Long id,
            @Valid @RequestBody SpiceAdjustmentRequest request
    ) {
        SpiceAdjustmentResult response = recipeAiAdjustmentService.getSpiceAdjustment(id, request);
        return ResponseEntity.ok(response);
    }
}
