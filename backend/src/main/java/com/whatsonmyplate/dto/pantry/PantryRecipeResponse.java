package com.whatsonmyplate.dto.pantry;

import com.whatsonmyplate.dto.recipe.RecipeResponse;

public class PantryRecipeResponse {

    private RecipeResponse recipe;
    private Integer matchedIngredients;
    private Integer totalIngredients;
    private Integer missingIngredients;
    private Integer matchPercentage;

    public PantryRecipeResponse() {
    }

    public PantryRecipeResponse(RecipeResponse recipe, Integer matchedIngredients, Integer totalIngredients, Integer missingIngredients, Integer matchPercentage) {
        this.recipe = recipe;
        this.matchedIngredients = matchedIngredients;
        this.totalIngredients = totalIngredients;
        this.missingIngredients = missingIngredients;
        this.matchPercentage = matchPercentage;
    }

    public RecipeResponse getRecipe() {
        return recipe;
    }

    public void setRecipe(RecipeResponse recipe) {
        this.recipe = recipe;
    }

    public Integer getMatchedIngredients() {
        return matchedIngredients;
    }

    public void setMatchedIngredients(Integer matchedIngredients) {
        this.matchedIngredients = matchedIngredients;
    }

    public Integer getTotalIngredients() {
        return totalIngredients;
    }

    public void setTotalIngredients(Integer totalIngredients) {
        this.totalIngredients = totalIngredients;
    }

    public Integer getMissingIngredients() {
        return missingIngredients;
    }

    public void setMissingIngredients(Integer missingIngredients) {
        this.missingIngredients = missingIngredients;
    }

    public Integer getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(Integer matchPercentage) {
        this.matchPercentage = matchPercentage;
    }
}
