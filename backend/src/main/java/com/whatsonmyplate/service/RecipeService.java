package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.ai.AiRecipeResult;
import com.whatsonmyplate.dto.recipe.IngredientItemDto;
import com.whatsonmyplate.dto.recipe.InstructionStepDto;
import com.whatsonmyplate.dto.recipe.RecipeCreateRequest;
import com.whatsonmyplate.dto.recipe.RecipePageResponse;
import com.whatsonmyplate.dto.recipe.RecipeResponse;
import com.whatsonmyplate.dto.recipe.RecipeUpdateRequest;
import com.whatsonmyplate.entity.Recipe;
import com.whatsonmyplate.entity.enums.RecipeSource;
import com.whatsonmyplate.entity.enums.SpiceLevel;
import com.whatsonmyplate.entity.model.IngredientItem;
import com.whatsonmyplate.entity.model.InstructionStep;
import com.whatsonmyplate.exception.ResourceNotFoundException;
import com.whatsonmyplate.repository.RecipeRepository;
import com.whatsonmyplate.repository.specification.RecipeSpecification;
import com.whatsonmyplate.service.ai.AiRecipeService;
import com.whatsonmyplate.service.external.TheMealDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private static final Logger logger = LoggerFactory.getLogger(RecipeService.class);

    public static final int MAX_PAGE_SIZE = 50;
    public static final int DEFAULT_PAGE_SIZE = 12;
    private static final Set<String> ALLOWED_SORT_FIELDS = new HashSet<>(Arrays.asList(
            "createdAt", "title", "prepTimeMinutes", "cookTimeMinutes", "updatedAt"
    ));

    private final RecipeRepository recipeRepository;
    private final TheMealDbClient theMealDbClient;
    private final AiRecipeService aiRecipeService;

    public RecipeService(
            RecipeRepository recipeRepository,
            TheMealDbClient theMealDbClient,
            AiRecipeService aiRecipeService
    ) {
        this.recipeRepository = recipeRepository;
        this.theMealDbClient = theMealDbClient;
        this.aiRecipeService = aiRecipeService;
    }

    @Transactional
    public RecipeResponse createRecipe(RecipeCreateRequest request) {
        Recipe recipe = new Recipe();
        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setImageUrl(request.getImageUrl());
        recipe.setSource(request.getSource() != null ? request.getSource() : RecipeSource.USER_CREATED);
        recipe.setSourceId(request.getSourceId());
        recipe.setIsAiGenerated(request.getIsAiGenerated() != null ? request.getIsAiGenerated() : false);
        recipe.setPrepTimeMinutes(request.getPrepTimeMinutes());
        recipe.setCookTimeMinutes(request.getCookTimeMinutes());
        recipe.setServings(request.getServings() != null ? request.getServings() : 1);
        recipe.setSpiceLevel(request.getSpiceLevel() != null ? request.getSpiceLevel() : SpiceLevel.MEDIUM);

        if (request.getIngredients() != null) {
            List<IngredientItem> ingredients = request.getIngredients().stream()
                    .map(dto -> new IngredientItem(dto.getName(), dto.getQuantity(), dto.getUnit()))
                    .collect(Collectors.toList());
            recipe.setIngredients(ingredients);
        }

        if (request.getInstructions() != null) {
            List<InstructionStep> instructions = request.getInstructions().stream()
                    .map(dto -> new InstructionStep(dto.getStep(), dto.getText()))
                    .collect(Collectors.toList());
            recipe.setInstructions(instructions);
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        return mapToResponse(savedRecipe);
    }

    @Transactional(readOnly = true)
    public RecipeResponse getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));
        return mapToResponse(recipe);
    }

    @Transactional(readOnly = true)
    public RecipePageResponse listRecipes(int page, int size, String sortParam) {
        return listRecipes(null, null, null, null, page, size, sortParam);
    }

    @Transactional
    public RecipePageResponse listRecipes(
            String q,
            String sourceStr,
            String spiceLevelStr,
            Integer maxPrepTime,
            int page,
            int size,
            String sortParam
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero.");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE + ".");
        }
        if (maxPrepTime != null && maxPrepTime < 0) {
            throw new IllegalArgumentException("maxPrepTime must be greater than or equal to 0.");
        }

        RecipeSource source = parseSource(sourceStr);
        SpiceLevel spiceLevel = parseSpiceLevel(spiceLevelStr);

        Sort sort = parseSort(sortParam);
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Recipe> spec = RecipeSpecification.withCriteria(q, source, spiceLevel, maxPrepTime);
        Page<Recipe> recipePage = recipeRepository.findAll(spec, pageable);

        // --- HYBRID DISCOVERY PIPELINE ---
        // If MySQL has 0 matching results on page 0 and a query string was given:
        if (recipePage.getTotalElements() == 0 && q != null && !q.trim().isEmpty() && page == 0) {
            boolean discovered = discoverAndCacheRecipe(q.trim());
            if (discovered) {
                recipePage = recipeRepository.findAll(spec, pageable);
            }
        }

        List<RecipeResponse> content = recipePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new RecipePageResponse(
                content,
                recipePage.getNumber(),
                recipePage.getSize(),
                recipePage.getTotalElements(),
                recipePage.getTotalPages(),
                recipePage.isLast()
        );
    }

    private boolean discoverAndCacheRecipe(String query) {
        // Step 2: Try External Provider (TheMealDB)
        try {
            List<Recipe> externalRecipes = theMealDbClient.searchRecipes(query);
            if (externalRecipes != null && !externalRecipes.isEmpty()) {
                logger.info("Caching {} recipes from TheMealDB for [{}]", externalRecipes.size(), query);
                recipeRepository.saveAll(externalRecipes);
                return true;
            }
        } catch (Exception e) {
            logger.warn("External recipe lookup failed for [{}]: {}", query, e.getMessage());
        }

        // Step 3: Fallback to Google Gemini AI
        try {
            logger.info("Falling back to Gemini AI recipe generation for [{}]", query);
            AiRecipeResult aiResult = aiRecipeService.generateRecipeFromText("Create a complete authentic recipe for " + query);
            if (aiResult != null) {
                Recipe aiRecipe = mapAiResultToRecipe(aiResult);
                recipeRepository.save(aiRecipe);
                logger.info("Cached AI generated recipe for [{}] into MySQL", query);
                return true;
            }
        } catch (Exception e) {
            logger.warn("Gemini AI fallback generation failed for [{}]: {}", query, e.getMessage());
        }

        return false;
    }

    private Recipe mapAiResultToRecipe(AiRecipeResult result) {
        Recipe recipe = new Recipe();
        recipe.setTitle(result.getDishName());
        recipe.setDescription(result.getDescription());
        recipe.setImageUrl(null);
        recipe.setSource(RecipeSource.AI_GENERATED);
        recipe.setIsAiGenerated(true);
        recipe.setPrepTimeMinutes(result.getPrepTimeMinutes() != null ? result.getPrepTimeMinutes() : 15);
        recipe.setCookTimeMinutes(result.getCookTimeMinutes() != null ? result.getCookTimeMinutes() : 25);
        recipe.setServings(result.getServings() != null ? result.getServings() : 4);
        recipe.setSpiceLevel(result.getSpiceLevel() != null ? result.getSpiceLevel() : SpiceLevel.MEDIUM);

        if (result.getIngredients() != null) {
            List<IngredientItem> ingredients = result.getIngredients().stream()
                    .map(i -> new IngredientItem(i.getName(), i.getQuantity(), i.getUnit()))
                    .collect(Collectors.toList());
            recipe.setIngredients(ingredients);
        }

        if (result.getInstructions() != null) {
            List<InstructionStep> instructions = result.getInstructions().stream()
                    .map(s -> new InstructionStep(s.getStep(), s.getText()))
                    .collect(Collectors.toList());
            recipe.setInstructions(instructions);
        }

        return recipe;
    }

    @Transactional
    public RecipeResponse updateRecipe(Long id, RecipeUpdateRequest request) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));

        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setImageUrl(request.getImageUrl());
        recipe.setSource(request.getSource());
        recipe.setSourceId(request.getSourceId());
        recipe.setIsAiGenerated(request.getIsAiGenerated());
        recipe.setPrepTimeMinutes(request.getPrepTimeMinutes());
        recipe.setCookTimeMinutes(request.getCookTimeMinutes());
        recipe.setServings(request.getServings());
        recipe.setSpiceLevel(request.getSpiceLevel());

        if (request.getIngredients() != null) {
            List<IngredientItem> ingredients = request.getIngredients().stream()
                    .map(dto -> new IngredientItem(dto.getName(), dto.getQuantity(), dto.getUnit()))
                    .collect(Collectors.toList());
            recipe.setIngredients(ingredients);
        }

        if (request.getInstructions() != null) {
            List<InstructionStep> instructions = request.getInstructions().stream()
                    .map(dto -> new InstructionStep(dto.getStep(), dto.getText()))
                    .collect(Collectors.toList());
            recipe.setInstructions(instructions);
        }

        Recipe updatedRecipe = recipeRepository.save(recipe);
        return mapToResponse(updatedRecipe);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found with id: " + id));

        try {
            recipeRepository.delete(recipe);
            recipeRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Cannot delete recipe because it is currently referenced by other records."
            );
        }
    }

    private RecipeSource parseSource(String sourceStr) {
        if (sourceStr == null || sourceStr.trim().isEmpty()) {
            return null;
        }
        try {
            return RecipeSource.valueOf(sourceStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid source: '" + sourceStr + "'. Allowed values are: " +
                            Arrays.stream(RecipeSource.values()).map(Enum::name).collect(Collectors.joining(", "))
            );
        }
    }

    private SpiceLevel parseSpiceLevel(String spiceLevelStr) {
        if (spiceLevelStr == null || spiceLevelStr.trim().isEmpty()) {
            return null;
        }
        try {
            return SpiceLevel.valueOf(spiceLevelStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid spiceLevel: '" + spiceLevelStr + "'. Allowed values are: " +
                            Arrays.stream(SpiceLevel.values()).map(Enum::name).collect(Collectors.joining(", "))
            );
        }
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sortParam.split(",");
        String field = parts[0].trim();
        Sort.Direction direction = Sort.Direction.DESC;

        if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.ASC;
        }

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: '" + field + "'. Allowed sort fields are: " + String.join(", ", ALLOWED_SORT_FIELDS)
            );
        }

        return Sort.by(direction, field);
    }

    private RecipeResponse mapToResponse(Recipe recipe) {
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
