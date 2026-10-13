package com.whatsonmyplate.controller;

import com.whatsonmyplate.dto.pantry.PantryItemCreateRequest;
import com.whatsonmyplate.dto.pantry.PantryItemResponse;
import com.whatsonmyplate.dto.pantry.PantryItemUpdateRequest;
import com.whatsonmyplate.dto.pantry.PantryRecipeResponse;
import com.whatsonmyplate.service.PantryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pantry")
public class PantryController {

    private final PantryService pantryService;

    public PantryController(PantryService pantryService) {
        this.pantryService = pantryService;
    }

    @GetMapping
    public ResponseEntity<List<PantryItemResponse>> getPantryItems(@AuthenticationPrincipal UserDetails userDetails) {
        List<PantryItemResponse> items = pantryService.getPantryItems(userDetails.getUsername());
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<PantryItemResponse> createPantryItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PantryItemCreateRequest request
    ) {
        PantryItemResponse item = pantryService.createPantryItem(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PantryItemResponse> updatePantryItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody PantryItemUpdateRequest request
    ) {
        PantryItemResponse item = pantryService.updatePantryItem(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePantryItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        pantryService.deletePantryItem(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recipes")
    public ResponseEntity<List<PantryRecipeResponse>> getPantryMatchingRecipes(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<PantryRecipeResponse> recipes = pantryService.findMatchingRecipes(userDetails.getUsername());
        return ResponseEntity.ok(recipes);
    }
}
