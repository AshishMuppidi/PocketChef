package com.whatsonmyplate.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.entity.model.IngredientItem;
import com.whatsonmyplate.entity.model.InstructionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TheMealDbClient {

    private static final Logger logger = LoggerFactory.getLogger(TheMealDbClient.class);
    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TheMealDbClient(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
        this.objectMapper = objectMapper;
    }

    public List<Recipe> searchRecipes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String url = "/search.php?s=" + encodedQuery;

            logger.info("Searching TheMealDB for: [{}]", query);
            String responseBody = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode mealsNode = root.get("meals");

            if (mealsNode == null || mealsNode.isNull() || !mealsNode.isArray() || mealsNode.isEmpty()) {
                logger.info("No recipes found on TheMealDB for query: [{}]", query);
                return Collections.emptyList();
            }

            List<Recipe> recipes = new ArrayList<>();
            for (JsonNode mealNode : mealsNode) {
                Recipe recipe = mapMealToRecipe(mealNode);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            }

            logger.info("Found {} recipes on TheMealDB for query: [{}]", recipes.size(), query);
            return recipes;
        } catch (Exception e) {
            logger.warn("Failed to query TheMealDB for query [{}]: {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private Recipe mapMealToRecipe(JsonNode node) {
        try {
            String idMeal = node.path("idMeal").asText(null);
            String strMeal = node.path("strMeal").asText(null);
            String strInstructions = node.path("strInstructions").asText(null);
            String strMealThumb = node.path("strMealThumb").asText(null);
            String strCategory = node.path("strCategory").asText("");
            String strArea = node.path("strArea").asText("");

            if (strMeal == null || strMeal.isBlank()) {
                return null;
            }

            Recipe recipe = new Recipe();
            recipe.setTitle(strMeal.trim());
            recipe.setDescription((strArea + " " + strCategory + " dish").trim());
            recipe.setImageUrl(strMealThumb);
            recipe.setSource(RecipeSource.THE_MEAL_DB);
            recipe.setSourceId(idMeal);
            recipe.setIsAiGenerated(false);
            recipe.setPrepTimeMinutes(15);
            recipe.setCookTimeMinutes(30);
            recipe.setServings(4);
            recipe.setSpiceLevel(SpiceLevel.MEDIUM);

            // Parse ingredients (strIngredient1..20, strMeasure1..20)
            List<IngredientItem> ingredients = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                String ingredient = node.path("strIngredient" + i).asText(null);
                String measure = node.path("strMeasure" + i).asText("");

                if (ingredient != null && !ingredient.trim().isEmpty()) {
                    ingredients.add(new IngredientItem(
                            ingredient.trim(),
                            1.0,
                            measure != null && !measure.isBlank() ? measure.trim() : "unit"
                    ));
                }
            }
            recipe.setIngredients(ingredients);

            // Parse instruction steps
            List<InstructionStep> instructions = new ArrayList<>();
            if (strInstructions != null && !strInstructions.isBlank()) {
                String[] rawSteps = strInstructions.split("\r?\n+");
                int stepNum = 1;
                for (String rawStep : rawSteps) {
                    String cleanText = rawStep.trim().replaceAll("^[0-9]+[.)]\\s*", "");
                    if (!cleanText.isEmpty()) {
                        instructions.add(new InstructionStep(stepNum++, cleanText));
                    }
                }
            }

            if (instructions.isEmpty()) {
                instructions.add(new InstructionStep(1, "Prepare ingredients and cook according to traditional method."));
            }
            recipe.setInstructions(instructions);

            return recipe;
        } catch (Exception e) {
            logger.warn("Failed to parse meal node: {}", e.getMessage());
            return null;
        }
    }
}
