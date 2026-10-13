package com.whatsonmyplate.dto.recipe;

import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class RecipeUpdateRequest {

    @NotBlank(message = "Recipe title is required")
    @Size(max = 255, message = "Recipe title cannot exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Recipe description cannot exceed 5000 characters")
    private String description;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;

    @NotNull(message = "Recipe source is required")
    private RecipeSource source = RecipeSource.USER_CREATED;

    @Size(max = 100, message = "Source ID cannot exceed 100 characters")
    private String sourceId;

    @NotNull(message = "isAiGenerated is required")
    private Boolean isAiGenerated = false;

    @PositiveOrZero(message = "Preparation time must be zero or positive")
    private Integer prepTimeMinutes;

    @PositiveOrZero(message = "Cook time must be zero or positive")
    private Integer cookTimeMinutes;

    @NotNull(message = "Servings is required")
    @Positive(message = "Servings must be at least 1")
    private Integer servings = 1;

    @NotNull(message = "Spice level is required")
    private SpiceLevel spiceLevel = SpiceLevel.MEDIUM;

    @NotEmpty(message = "At least one ingredient is required")
    @Valid
    private List<IngredientItemDto> ingredients = new ArrayList<>();

    @NotEmpty(message = "At least one instruction step is required")
    @Valid
    private List<InstructionStepDto> instructions = new ArrayList<>();

    public RecipeUpdateRequest() {
    }

    public RecipeUpdateRequest(String title, String description, String imageUrl, RecipeSource source, String sourceId, Boolean isAiGenerated, Integer prepTimeMinutes, Integer cookTimeMinutes, Integer servings, SpiceLevel spiceLevel, List<IngredientItemDto> ingredients, List<InstructionStepDto> instructions) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.source = source != null ? source : RecipeSource.USER_CREATED;
        this.sourceId = sourceId;
        this.isAiGenerated = isAiGenerated != null ? isAiGenerated : false;
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.servings = servings != null ? servings : 1;
        this.spiceLevel = spiceLevel != null ? spiceLevel : SpiceLevel.MEDIUM;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.instructions = instructions != null ? instructions : new ArrayList<>();
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
}
