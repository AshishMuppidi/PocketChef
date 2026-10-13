package com.whatsonmyplate.dto.recipe;

import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecipeResponse {

    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private RecipeSource source;
    private String sourceId;
    private Boolean isAiGenerated;
    private Integer prepTimeMinutes;
    private Integer cookTimeMinutes;
    private Integer servings;
    private SpiceLevel spiceLevel;
    private List<IngredientItemDto> ingredients = new ArrayList<>();
    private List<InstructionStepDto> instructions = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RecipeResponse() {
    }

    public RecipeResponse(Long id, String title, String description, String imageUrl, RecipeSource source, String sourceId, Boolean isAiGenerated, Integer prepTimeMinutes, Integer cookTimeMinutes, Integer servings, SpiceLevel spiceLevel, List<IngredientItemDto> ingredients, List<InstructionStepDto> instructions, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.source = source;
        this.sourceId = sourceId;
        this.isAiGenerated = isAiGenerated;
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.servings = servings;
        this.spiceLevel = spiceLevel;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.instructions = instructions != null ? instructions : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public RecipeSource getSource() {
        return source;
    }

    public void setSource(RecipeSource source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Boolean getIsAiGenerated() {
        return isAiGenerated;
    }

    public void setIsAiGenerated(Boolean isAiGenerated) {
        this.isAiGenerated = isAiGenerated;
    }

    public Integer getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public void setPrepTimeMinutes(Integer prepTimeMinutes) {
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public Integer getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public void setCookTimeMinutes(Integer cookTimeMinutes) {
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public SpiceLevel getSpiceLevel() {
        return spiceLevel;
    }

    public void setSpiceLevel(SpiceLevel spiceLevel) {
        this.spiceLevel = spiceLevel;
    }

    public List<IngredientItemDto> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientItemDto> ingredients) {
        this.ingredients = ingredients;
    }

    public List<InstructionStepDto> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<InstructionStepDto> instructions) {
        this.instructions = instructions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
